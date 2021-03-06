/*-
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */

package org.apache.griffin.core.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.griffin.core.measure.DataConnector;
import org.apache.griffin.core.measure.EvaluateRule;
import org.apache.griffin.core.measure.Measure;
import org.apache.griffin.core.measure.repo.MeasureRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest(SparkSubmitJob.class)
@RunWith(SpringRunner.class)
public class SparkSubmitJobTest{

    private SparkSubmitJob ssj;

//    @Autowired
    @MockBean
    MeasureRepo measureRepo;

    @Before
    public void setUp() throws IOException {
        ssj=new SparkSubmitJob();
        ssj.measureRepo=mock(MeasureRepo.class);
        ssj.restTemplate= mock(RestTemplate.class);
    }

    @Test
    public void test_execute() throws Exception {
        JobExecutionContext context=mock(JobExecutionContext.class);
        JobDetail jd = mock(JobDetail.class);
        when(context.getJobDetail()).thenReturn(jd);

        JobDataMap jdmap = mock(JobDataMap.class);
        when(jd.getJobDataMap()).thenReturn(jdmap);

        when(jdmap.getString("measure")).thenReturn("bevssoj");
        when(jdmap.getString("sourcePat")).thenReturn("YYYYMMDD-HH");
        when(jdmap.getString("targetPat")).thenReturn("YYYYMMDD-HH");
        when(jdmap.getString("dataStartTimestamp")).thenReturn("1460174400000");
        when(jdmap.getString("lastTime")).thenReturn("");
        when(jdmap.getString("periodTime")).thenReturn("10");
        Measure measure = createATestMeasure("viewitem_hourly","bullseye");
        when(ssj.measureRepo.findByName("bevssoj")).thenReturn(measure);

        RestTemplate restTemplate =Mockito.mock(RestTemplate.class);
//        PowerMockito.whenNew(RestTemplate.class).withAnyArguments().thenReturn(restTemplate);
        String uri=ssj.uri;
        SparkJobDO sparkJobDO= Mockito.mock(SparkJobDO.class);
//        PowerMockito.when(restTemplate.postForObject(uri, sparkJobDO, String.class)).thenReturn(null);
        when(restTemplate.postForObject(uri, sparkJobDO, String.class)).thenReturn(null);
        ssj.execute(context);

        long currentSystemTimestamp=System.currentTimeMillis();
        long currentTimstamp = ssj.setCurrentTimestamp(currentSystemTimestamp);

        verify(ssj.measureRepo).findByName("bevssoj");
        verify(jdmap,atLeast(2)).put("lastTime",currentTimstamp+"");

        when(ssj.measureRepo.findByName("bevssoj")).thenReturn(null);
        ssj.execute(context);

        when(ssj.measureRepo.findByName("bevssoj")).thenReturn(measure);
        String result="{\"id\":8718,\"state\":\"starting\",\"appId\":null,\"appInfo\":{\"driverLogUrl\":null,\"sparkUiUrl\":null},\"log\":[]}";
        when(restTemplate.postForObject(uri, sparkJobDO, String.class)).thenReturn(result);
        ssj.execute(context);
    }

    private Measure createATestMeasure(String name,String org)throws IOException,Exception{
        HashMap<String,String> configMap1=new HashMap<>();
        configMap1.put("database","default");
        configMap1.put("table.name","test_data_src");
        HashMap<String,String> configMap2=new HashMap<>();
        configMap2.put("database","default");
        configMap2.put("table.name","test_data_tgt");
        String configJson1 = new org.codehaus.jackson.map.ObjectMapper().writeValueAsString(configMap1);
        String configJson2 = new org.codehaus.jackson.map.ObjectMapper().writeValueAsString(configMap2);

        DataConnector source = new DataConnector(DataConnector.ConnectorType.HIVE, "1.2", configJson1);
        DataConnector target = new DataConnector(DataConnector.ConnectorType.HIVE, "1.2", configJson2);

        String rules = "$source.uage > 100 AND $source.uid = $target.uid AND $source.uage + 12 = $target.uage + 10 + 2 AND $source.udes + 11 = $target.udes + 1 + 1";

        EvaluateRule eRule = new EvaluateRule(1,rules);

        Measure measure = new Measure(name,"bevssoj description", Measure.MearuseType.accuracy, org, source, target, eRule,"test1");

        return measure;
    }

    @Test
    public void test_genPartitions(){
        String[] patternItemSet={"YYYYMMDD","HH"};
        String[] partitionItemSet={"date","hour"};
        long timestamp=System.currentTimeMillis();
        Map<String,String> par=ssj.genPartitions(patternItemSet,partitionItemSet,timestamp);
        Map<String,String> verifyMap=new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
        verifyMap.put("date",sdf.format(new Date(timestamp)));
        SimpleDateFormat sdf1 = new SimpleDateFormat("HH");
        verifyMap.put("hour",sdf1.format(new Date(timestamp)));
        assertEquals(verifyMap,par);
    }

    @Test
    public void test_setDataConnectorPartitions(){
        DataConnector dc=mock(DataConnector.class);
        String[] patternItemSet={"YYYYMMDD","HH"};
        String[] partitionItemSet={"date","hour"};
        long timestamp=System.currentTimeMillis();
        ssj.setDataConnectorPartitions(dc,patternItemSet,partitionItemSet,timestamp);
        Map<String,String> map=new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("HH");
        map.put("partitions","date="+sdf.format(new Date(timestamp))+", hour="+sdf1.format(new Date(timestamp)));
        try {
            verify(dc).setConfig(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_setCurrentTimestamp(){
        long timestamp=System.currentTimeMillis();
        ssj.eachJoblastTimestamp="";
        System.out.println(ssj.setCurrentTimestamp(timestamp));
        ssj.eachJoblastTimestamp=(timestamp-1000)+"";
        ssj.periodTime="1000";
        System.out.println(ssj.setCurrentTimestamp(timestamp));
    }

    @Test
    public void test_setSparkJobDO(){
        ssj=mock(SparkSubmitJob.class);
        doNothing().when(ssj).setSparkJobDO();
    }

    @Test
    public void test_getsparkJobProperties(){
        ssj=mock(SparkSubmitJob.class);
        try {
            when(ssj.getsparkJobProperties()).thenReturn(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
