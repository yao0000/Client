package com.utar.client.ui.home;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.utar.client.R;
import com.utar.client.ui.home.transaction.TransactionViewHolder;

public class TransactionActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private static final String TAG = "TransactionActivity";
    private DatabaseReference databaseReference;
    private List<Transaction> transactionList = new ArrayList<>(), searchList;

    private DatePickerDialog startDatePickerDialog, endDatePickerDialog;
    private AlertDialog dialog;

    private ImageView iv_filter;
    private TextView tv_start_date, tv_end_date;
    private RecyclerView recyclerView;

    private static TransactionActivity transactionActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        transactionActivity = this;

        recyclerView = findViewById(R.id.transaction_list);
        iv_filter = findViewById(R.id.iv_filter);
        iv_filter.setOnClickListener(v -> v.showContextMenu());
        registerForContextMenu(iv_filter);

        String userID = FirebaseAuth.getInstance().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("transactions");
        initDatePickDialog();

        databaseReference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    transactionList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Transaction transaction = dataSnapshot.getValue(Transaction.class);
                        transactionList.add(0, transaction);
                    }
                    displayList(transactionList);
                }
                else {
                    toast(getString(R.string.noRecord));
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                toast(error.getMessage());
            }
        });
    }

    public static TransactionActivity getInstance(){
        return transactionActivity;
    }

    public static String getIdString(int resId){
        return transactionActivity.getString(resId);
    }

    private void displayList(List list){
        recyclerView.setLayoutManager(new LinearLayoutManager(TransactionActivity.this));
        recyclerView.setAdapter(new RecyclerView.Adapter<TransactionViewHolder>() {

            @Override
            public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
                return new TransactionViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(TransactionViewHolder holder, int position) {
                Transaction transaction = (Transaction) list.get(position);
                holder.bind(transaction);
            }

            @Override
            public int getItemCount() {
                return list.size();
            }
        });
    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void initDatePickDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.layout_date_picker, null);
        TextView tv_search = view.findViewById(R.id.date_tv_search);

        tv_start_date = view.findViewById(R.id.tv_start_date);
        tv_end_date = view.findViewById(R.id.tv_end_date);
        Button btn_start_date = view.findViewById(R.id.btn_start_date);
        Button btn_end_date = view.findViewById(R.id.btn_end_date);

        //to initialise to current date
        Calendar currentDate = Calendar.getInstance();
        startDatePickerDialog = DatePickerDialog.newInstance(null, currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
        endDatePickerDialog = DatePickerDialog.newInstance(null, currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));

        //show date picker dialog
        btn_start_date.setOnClickListener(v -> startDatePickerDialog.show(getSupportFragmentManager(), "startDatePicker"));
        btn_end_date.setOnClickListener(v -> endDatePickerDialog.show(getSupportFragmentManager(), "endDatePicker"));

        tv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startDate = String.valueOf(tv_start_date.getText());
                String endDate = String.valueOf(tv_end_date.getText());

                if(startDate.equals(getResources().getString(R.string.selectDate)) || endDate.equals(getResources().getString(R.string.selectDate))){
                    toast(getResources().getString(R.string.pleaseFillInDate));
                    return;
                }

                startDate = startDate + " 00:00:00";
                endDate = endDate + " 23:59:59";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm:ss");

                searchList = new ArrayList<>();
                try {
                    long startTime = simpleDateFormat.parse(startDate).getTime();
                    long endTime = simpleDateFormat.parse(endDate).getTime();
                    if(startTime > endTime){
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransactionActivity.this);
                        alertDialog.setTitle(getString(R.string.alert));
                        alertDialog.setMessage(getString(R.string.date_duration_error));
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
                            toast(getResources().getString(R.string.noRecord));
                        }else {
                            displayList(searchList);
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
    }


    private boolean isInRange(Transaction transaction, long startTime, long endTime){
        return ((transaction.getTimestamp() > startTime) && (transaction.getTimestamp() < endTime));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v.getId() == R.id.iv_filter){
            menu.add(0, 1, 0, getResources().getString(R.string.filter));
            menu.add(0, 2, 0, getResources().getString(R.string.reset));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case 1:{
                dialog.show();
                break;
            }
            case 2:{
                displayList(transactionList);
                break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String month[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};
        String selectedDate = dayOfMonth + " " + month[monthOfYear] + " " + year;

        if(view == startDatePickerDialog){
            tv_start_date.setText(selectedDate);
        }
        else if(view == endDatePickerDialog){
            tv_end_date.setText(selectedDate);
        }
    }
}