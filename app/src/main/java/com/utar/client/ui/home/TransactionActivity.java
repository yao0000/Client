package com.utar.client.ui.home;


import com.utar.client.ui.home.transaction.FilterDate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Date;
import java.util.List;

import com.utar.client.R;
import com.utar.client.ui.home.transaction.FilterPopupWindows;
import com.utar.client.ui.home.transaction.FilterTransactionType;
import com.utar.client.ui.home.transaction.TransactionViewHolder;

public class TransactionActivity extends AppCompatActivity implements View.OnClickListener
        , FilterDate.FilterListListener, FilterTransactionType.FilterTypeListener {

    private static final String TAG = "TransactionActivity";
    private DatabaseReference databaseReference;
    private List<Transaction> transactionList = new ArrayList<>(), displayList;
    private TextView tv_range;
    private RecyclerView recyclerView;

    private static TransactionActivity transactionActivity;

    private FilterDate filterDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        transactionActivity = this;
        filterDate = new FilterDate(TransactionActivity.this);

        recyclerView = findViewById(R.id.transaction_list);
        findViewById(R.id.iv_filter).setOnClickListener(this::onClick);
        findViewById(R.id.tv_date_range).setOnClickListener(this::onClick);
        findViewById(R.id.backBtn).setOnClickListener(this::onClick);
        tv_range = findViewById(R.id.tv_range);
        tv_range.setText(getDateString(getCalculatedTimestamp(-29, START_TIMESTAMP))
                + " - " + getDateString(getCalculatedTimestamp(0, END_TIMESTAMP)));

        databaseReference = FirebaseDatabase.getInstance().getReference("transactions");
        databaseReference
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            transactionList = new ArrayList<>();
                            long todayTimestamp = getCalculatedTimestamp(0, END_TIMESTAMP);
                            long previous90daysTimestamp = getCalculatedTimestamp(-90, START_TIMESTAMP);
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Transaction transaction = dataSnapshot.getValue(Transaction.class);

                                //only read last 90 day's transaction record
                                if(isInRange(transaction, previous90daysTimestamp, todayTimestamp)){
                                    transactionList.add(0, transaction);
                                }
                            }

                            displayList = new ArrayList<>();
                            long previous30days = getCalculatedTimestamp(-30, START_TIMESTAMP);

                            for (int i = transactionList.size() - 1; i >= 0; i--) {
                                if (isInRange(transactionList.get(i), previous30days, todayTimestamp)) {
                                    displayList.add(0, transactionList.get(i));
                                }
                            }
                            displayList(displayList);
                        } else {
                            toast(getString(R.string.noRecord));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        toast(error.getMessage());
                    }
                });
    }

    public static String getDateString(long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
        Date date = new Date(timestamp);
        return simpleDateFormat.format(date);
    }

    public static final int START_TIMESTAMP = 0;
    public static final int END_TIMESTAMP = 1;
    public static long getCalculatedTimestamp(int dayCount, int mode) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        if(mode == START_TIMESTAMP){
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        else if(mode == END_TIMESTAMP){
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
        }

        calendar.add(Calendar.DAY_OF_MONTH, dayCount);
        return calendar.getTimeInMillis();
    }

    public static TransactionActivity getInstance() {
        return transactionActivity;
    }

    public static String getIdString(int resId) {
        return transactionActivity.getString(resId);
    }

    private void displayList(List list) {

        if (list.isEmpty()) {
            toast(getString(R.string.noRecord));
            return;
        }

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

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public List getTransactionList() {
        return transactionList;
    }

    public boolean isInRange(Transaction transaction, long startTime, long endTime) {
        return ((transaction.getTimestamp() >= startTime) && (transaction.getTimestamp() <= endTime));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_filter: {
                new FilterPopupWindows(TransactionActivity.this, findViewById(R.id.mainLayout));
                break;
            }
            case R.id.tv_date_range: {
                filterDate.show();
                break;
            }
            case R.id.backBtn:{
                onBackPressed();
                break;
            }
        }
    }

    @Override
    public void setFilteredList(List list) {
        displayList = list;
        displayList(displayList);
    }

    @Override
    public List getCurrentDisplayList() {
        return displayList;
    }

    @Override
    public void displayFilteredList(List list) {
        displayList(list);
    }
}