package com.regtech.regcore.sierradevops.prometheus.services;


import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.stylesheets.LinkStyle;

@Slf4j
@Component
public class MetricsAggregator {

   public  Map <String, String> records;

   //@Value("${app.name}")
   public static final String APPGREP = "appname=";

    //@Value("${app.port}")
    public static final String PORTGREP = "appport=";

   // @Value("${app.cmd:bash,-c,ps,-ef,|,grep,java,|,grep,java}")
    private  String cmdLine ="bash,-c,ps,-ef,|,grep,java,|,grep,java";
    private  final List<String> cmdLineList = Arrays.asList(cmdLine.split(","));


   public void setRecords (Map<String, String> records) {
        this.records = records;
    }

    private String bufferedresult;

    public String getBufferedOutput () {
        return bufferedresult;
    }
    private void  setBufferedOutput (String data) {
        bufferedresult = data;
    }
    public void execute ()
    {
        log.info("do nothing");
        //get app and port list

        HashMap<String, String > appListAndPort = new HashMap<String, String >();
        //appListAndPort.put("someapp","1100");
        //MetricsAggregator.fetchAppList (cmdLineList);
        Map<String, String> records =  MetricsAggregator.fetchAppList (cmdLineList);
        if (records != null)
            this.setBufferedOutput(MetricsAggregator.scrapePrometheusEndpoint(records).toString());

        //this.setBufferedOutput(MetricsAggregator.scrapePrometheusEndpoint(appListAndPort).toString());// = MetricsAggregator.scrapePrometheusEndpoint(appListAndPort).toString();
    }
    private static StringBuilder scrapePrometheusEndpoint ( Map<String, String> records) {

        StringBuilder builder = new StringBuilder();

        for (var record : records.entrySet()) {
            String app = record.getKey();
            String port = record.getValue();
            log.info ("processing app {} on port {}",app,port);

            try {
                URL url = new URL("http://localhost:" + port + "/metrics");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try {
                        BufferedReader br =
                                new BufferedReader(
                                        new InputStreamReader(con.getInputStream()));
                        String input;
                        while ((input = br.readLine()) != null) {
                            if (input.trim().length() > 0 ) {
                                StringBuilder strb = new StringBuilder(input);
                                if (strb.substring(0,1).compareToIgnoreCase("#") != 0) {
                                    if (strb.indexOf("{") < 0) {
                                        int loc = strb.lastIndexOf(" ");
                                        strb.replace(loc, loc + 1, "{abc=\"def\",lmn=\"opq\"} ");
                                    } else if (strb.indexOf("}") > 0) {
                                        int loc = strb.lastIndexOf("}");
                                        strb.replace(loc, loc + 1, ",abc=\"def\",lmn=\"opq\"} ");
                                    }
                                }
                                builder.append(strb + "\n");
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception exception) {
                log.error("Error parsing the record for the payload ", builder.toString());
                exception.printStackTrace();
            }
        }
        return builder;
    }

    private static Map <String, String> fetchAppList (final List<String> cmd) {

        ProcessBuilder processBuilder = new ProcessBuilder();
        Map<String, String>  appsAndPorts = new HashMap<String, String >();

        log.info("cmd is {}", cmd);
        processBuilder.command(cmd);
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                //log.info("cmd is {}", line);

                // check and extract what is needed to a map
                int appIndex = line.indexOf(APPGREP);
                int portIndex = line.indexOf(PORTGREP);

                if (appIndex > -1) {
                    String app = line.substring(appIndex+APPGREP.length(), line.indexOf(" ", appIndex));
                    //log.info("app name is {}",app);
                    //log.info("others name is {} {}",portIndex, line );
                    String port = line.substring(portIndex+PORTGREP.length(), line.indexOf("#", portIndex));

                    if (app.length() > 3 && port.length() > 3)
                        appsAndPorts.put(app, port); // add to map
                    //log.info("match {} {}",app, port);
                }
            }
            if (appsAndPorts.size() !=0)
                return appsAndPorts;

        } catch (IOException e) {
            e.printStackTrace();
        }

         /*   int exitVal = process.waitFor();
            if (exitVal == 0) {
                return output;
            }



        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

          */
        return null;
    }

}
