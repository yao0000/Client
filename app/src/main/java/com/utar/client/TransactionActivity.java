package com.utar.client;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utar.client.data.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class TransactionActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private List<Transaction> transactionList = new ArrayList<>(), searchList;


    private int displayHeight, displayWidth;
    private int blue;

    private DatePickerDialog startDatePickerDialog, endDatePickerDialog;
    private TextView tv_startDate, tv_endDate;
    private AlertDialog dialog;


    private ScrollView transactionScrollView;
    private LinearLayout listLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        //System Initialise
        getSupportActionBar().hide();
        displayHeight = getResources().getDisplayMetrics().heightPixels;
        displayWidth = getResources().getDisplayMetrics().widthPixels;
        String userID = FirebaseAuth.getInstance().getUid();
        //FirebaseDatabase.getInstance().getReference("user").child(userID).keepSynced(true);
        databaseReference = FirebaseDatabase.getInstance().getReference("transactions");
        blue = getResources().getColor(R.color.soft_blue);
        initDatePickDialog();

        // Retrieve ID
        transactionScrollView = findViewById(R.id.wholeTransaction);
        Button btn_search = findViewById(R.id.transaction_btn_search);
        Button btn_reset = findViewById(R.id.transaction_btn_reset);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTransactionView(transactionList);
                transactionScrollView.addView(listLayout);
            }
        });

        databaseReference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    transactionList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Transaction transaction = dataSnapshot.getValue(Transaction.class);
                        transactionList.add(transaction);
                    }

                    //sorting
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        transactionList.sort(Comparator.comparing(Transaction::getTimestamp));
                    }
                    //setView(showList(transactionList));
                    setTransactionView(transactionList);
                    transactionScrollView.addView(listLayout);

                }
                else {
                    toast("No record found");
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                toast(error.getMessage());
            }
        });

    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void setTransactionView(List<Transaction> list){
        if(listLayout != null) {
            if (listLayout.getParent() != null) {
                ((ScrollView) listLayout.getParent()).removeView(listLayout);
            }
        }

        //For whole transaction list
        listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);
        listLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        listLayout.setPadding(0,0,10,0);

        for(int i = list.size() - 1; i >= 0; i--){
            Transaction transaction = list.get(i);

            //for single transaction container
            LinearLayout singleTransaction = new LinearLayout(this);
            singleTransaction.setOrientation(LinearLayout.VERTICAL);
            singleTransaction.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

            // Transaction display layout:
            // [Name]   [Amount]
            // [Date]   [Type]

            // name textview
            TextView tv_name = new TextView(this);
            tv_name.setText(transaction.getObjectName());
            tv_name.setTextSize(displayWidth * displayHeight / 100000 - 5);
            tv_name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            tv_name.setTextColor(Color.BLACK);
            tv_name.setLayoutParams(new ViewGroup.LayoutParams((int) (displayWidth * 0.6), ViewGroup.LayoutParams.WRAP_CONTENT));
            tv_name.setGravity(Gravity.LEFT);

            TextView tv_amount = new TextView(this);
            tv_amount.setTextSize(displayWidth * displayHeight / 100000 - 5);
            tv_amount.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            tv_amount.setLayoutParams(new ViewGroup.LayoutParams((int) (displayWidth * 0.35), ViewGroup.LayoutParams.MATCH_PARENT));
            tv_amount.setGravity(Gravity.RIGHT);
            if(transaction.getType().equals(Transaction.PAYMENT) || transaction.getType().equals(Transaction.TRANSFER_OUT)){
                tv_amount.setText(String.format("- RM%.2f", transaction.getAmount()));
                tv_amount.setTextColor(getResources().getColor(R.color.dark_red));
            }
            else{
                tv_amount.setText(String.format("RM%.2f",transaction.getAmount()));
                tv_amount.setTextColor(getResources().getColor(R.color.dark_green));
            }

            //container design for single transaction details
            GridLayout gl = new GridLayout(this);
            gl.setOrientation(GridLayout.HORIZONTAL);
            gl.setRowCount(1);
            gl.setColumnCount(2);
            gl.addView(tv_name);
            gl.addView(tv_amount);
            singleTransaction.addView(gl);

            // Transaction display layout:
            // [Name]   [Amount]  => Configuration done
            // [Date]   [Type]

            TextView tv_time = new TextView(this);
            tv_time.setText(transaction.getTime());
            tv_time.setTextSize(displayWidth * displayHeight / 100000 - 8);
            tv_time.setLayoutParams(new ViewGroup.LayoutParams((int) (displayWidth * 0.4), ViewGroup.LayoutParams.MATCH_PARENT));
            tv_time.setTextColor(Color.BLACK);
            tv_time.setPadding(0,10,0,10);
            tv_time.setGravity(Gravity.LEFT);

            TextView tv_type = new TextView(this);
            tv_type.setText(transaction.getType());
            tv_type.setTextSize(displayWidth * displayHeight / 100000 - 8);
            tv_type.setLayoutParams(new ViewGroup.LayoutParams((int) (displayWidth * 0.55), ViewGroup.LayoutParams.MATCH_PARENT));
            tv_type.setTextColor(Color.BLACK);
            tv_type.setGravity(Gravity.RIGHT);

            gl = new GridLayout(this);
            gl.setOrientation(GridLayout.HORIZONTAL);
            gl.setRowCount(1);
            gl.setColumnCount(2);
            gl.addView(tv_time);
            gl.addView(tv_type);
            singleTransaction.addView(gl);

            View separator = new View(this);
            separator.setBackgroundColor(Color.BLACK);
            separator.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (displayHeight * 0.001)));
            separator.setPadding(0,15,0,15);
            singleTransaction.addView(separator);

            listLayout.addView(singleTransaction);
        }

    }

    private void initDatePickDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.layout_date_picker, null);
        TextView tv_submit = view.findViewById(R.id.date_tv_submit);
        tv_startDate = view.findViewById(R.id.picker_startDate);
        tv_endDate = view.findViewById(R.id.picker_endDate);

        //show date picker dialog
        tv_startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDatePickerDialog.show();
            }
        });
        tv_endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endDatePickerDialog.show();
            }
        });

        tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startDate = String.valueOf(tv_startDate.getText());
                String endDate = String.valueOf(tv_endDate.getText());

                if(startDate.equals("Mmm DD YYYY      \u25BC") || endDate.equals("Mmm DD YYYY      \u25BC")){
                    toast("Please select date");
                    return;
                }

                startDate = startDate.substring(0, 11) + " 00:00:00";
                endDate = endDate.substring(0, 11) + " 23:59:59";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd yyyy hh:mm:ss");

                searchList = new ArrayList<>();
                try {
                    long startTime = simpleDateFormat.parse(startDate).getTime();
                    long endTime = simpleDateFormat.parse(endDate).getTime();
                    if(startTime > endTime){
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransactionActivity.this);
                        alertDialog.setTitle("Alert");
                        alertDialog.setMessage("End date should be greater than Start date");
                        alertDialog.setPositiveButton( "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogOwn, int which) {
                                        dialogOwn.dismiss();
                                    }
                                });
                        alertDialog.create().show();
                        return;

                    }
                    else{
                        for(int i = transactionList.size()-1; i >= 0; i--){
                            if(isInRange(transactionList.get(i), startTime, endTime)){
                                searchList.add(transactionList.get(i));
                            }
                        }

                        if(searchList.size() == 0){
                            toast("No record found!");
                        }else {
                            setTransactionView(searchList);
                            transactionScrollView.addView(listLayout);
                            dialog.dismiss();
                        }
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
        });
        alert.setView(view);
        dialog = alert.create();

        initDatePicker();
    }


    private String makeDateString(int day, int month, int year){
        String strMonth;
        switch(month){
            case 1: {
                strMonth = "Jan";
                break;
            }
            case 2:{
                strMonth = "Feb";
                break;
            }
            case 3:{
                strMonth = "Mar";
                break;
            }
            case 4:{
                strMonth = "Apr";
                break;
            }
            case 5:{
                strMonth = "May";
                break;
            }
            case 6:{
                strMonth = "Jun";
                break;
            }
            case 7:{
                strMonth = "Jul";
                break;
            }
            case 8:{
                strMonth = "Aug";
                break;
            }
            case 9:{
                strMonth = "Sep";
                break;
            }
            case 10:{
                strMonth = "Oct";
                break;
            }
            case 11:{
                strMonth = "Nov";
                break;
            }
            default:{
                strMonth = "Dec";

            }
        }

        return strMonth + " " + day + " " + year;
    }

    private void initDatePicker(){
        DatePickerDialog.OnDateSetListener startDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month += 1;
                String date = makeDateString(day, month, year);
                tv_startDate.setText(date + "      \u25BC");
            }
        };

        DatePickerDialog.OnDateSetListener endDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month += 1;
                String date = makeDateString(day, month, year);
                tv_endDate.setText(date + "      \u25BC");
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        startDatePickerDialog = new DatePickerDialog(this, style, startDateSetListener, year, month, day);
        endDatePickerDialog = new DatePickerDialog(this, style, endDateSetListener, year, month, day);
    }

    private boolean isInRange(Transaction transaction, long startTime, long endTime){
        return ((transaction.getTimestamp() > startTime) && (transaction.getTimestamp() < endTime));
    }
}