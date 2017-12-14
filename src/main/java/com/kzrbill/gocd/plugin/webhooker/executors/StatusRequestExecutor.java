/*
 * Copyright 2016 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kzrbill.gocd.plugin.webhooker.executors;

import com.kzrbill.gocd.plugin.webhooker.PluginRequest;
import com.kzrbill.gocd.plugin.webhooker.RequestExecutor;
import com.kzrbill.gocd.plugin.webhooker.loggers.LoggerProxy;
import com.kzrbill.gocd.plugin.webhooker.requests.StatusRequest;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.HashMap;

public class StatusRequestExecutor implements RequestExecutor {

    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private final StatusRequest request;
    private final PluginRequest pluginRequest;
    private final LoggerProxy logger;

    public StatusRequestExecutor(StatusRequest request, PluginRequest pluginRequest) {
        this.request = request;
        this.pluginRequest = pluginRequest;
        this.logger = new LoggerProxy();
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        HashMap<String, Object> responseJson = new HashMap<>();
        try {
            sendNotification();
            responseJson.put("status", "success");
        } catch (Exception e) {
            responseJson.put("status", "failure");
            responseJson.put("message", e.getMessage());
        }
        return new DefaultGoPluginApiResponse(200, GSON.toJson(responseJson));
    }

    protected void sendNotification() throws Exception {
        // TODO: Implement this. The request.pipeline object has all the details about the pipeline, materials, stages and jobs
        // If you need access to settings like API keys, URLs, then call PluginRequest#getPluginSettings
        // PluginSettings pluginSettings = pluginRequest.getPluginSettings();

        logger.info("Send notification");

        try {
            this.postStageToApi();
        } catch (Exception e) {
            logger.info(e.toString());
            throw e;
        }
    }

    private void postStageToApi() throws Exception {
        String stageStatusJson = GSON.toJson(request);
        HttpResponse<JsonNode> jsonResponse = Unirest.post("http://localhost:3000/api/notifications/status")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(stageStatusJson)
                .asJson();
    }
}