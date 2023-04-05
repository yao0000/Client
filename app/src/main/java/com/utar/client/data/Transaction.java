package com.utar.client.data;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class Transaction {

  /*** Transaction Type ***/
  public final static String RELOAD = "Reload";                   //Green Color
  public final static String PAYMENT = "Payment";                 //Red Color
  public final static String TRANSFER_IN = "Receive Transfer";    //Green Color
  public final static String TRANSFER_OUT = "Transfer Out";       //Red Color
  public final static String WITHDRAW = "Withdraw";               //Red

  private String objectName;
  private double amount;
  private String type;
  private String time;
  private long timestamp;

  public Transaction(String objectName, double amount, String type){
    this.timestamp = System.currentTimeMillis();
    this.time = new SimpleDateFormat("d MMM, HH:mm").format(new Date(timestamp));
    this.objectName = objectName;
    this.amount = amount;
    this.type = type;
  }

  public Transaction() {
  }

  public long getTimestamp(){
    return timestamp;
  }

  public String getObjectName() {
    return objectName;
  }

  public double getAmount() {
    return amount;
  }

  public String getType() {
    return type;
  }

  public String getTime() {
    return time;
  }
}