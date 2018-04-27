package com.example.mateusz.homesecurity;

import android.util.Log;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;

import java.util.ArrayList;

public class DynamoDBManager {

    private static final String TAG = "DynamoDBManager";

    /*
     * Retrieves the table description and returns the table status as a string.
     */
    public static String getTestTableStatus() {

        try {
            AmazonDynamoDBClient ddb = MQTTActivity.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TEST_TABLE_NAME);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            MQTTActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }


    /*
     * Scans the table and returns temperature.
     */
    public static ArrayList<UserPreference> getTemperature() {

        AmazonDynamoDBClient ddb = MQTTActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        try {
            PaginatedScanList<UserPreference> result = mapper.scan(
                    UserPreference.class, scanExpression);

            ArrayList<UserPreference> resultList = new ArrayList<UserPreference>();
            for (UserPreference up : result) {
                resultList.add(up);
            }
            return resultList;

        } catch (AmazonServiceException ex) {
            MQTTActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    @DynamoDBTable(tableName = Constants.TEST_TABLE_NAME)
    public static class UserPreference {
        private String DeviceID;
        private String temperature;


        @DynamoDBHashKey(attributeName = "DeviceID")
        public String getDeviceID() {
            return DeviceID;
        }
        public void setDeviceID(String DeviceID) { this.DeviceID = DeviceID;}

        @DynamoDBAttribute(attributeName = "temperature")
        public String getDBTemperature() { return temperature; }
        public void setDBTemperature(String temperature) { this.temperature = temperature; }

    }
}
