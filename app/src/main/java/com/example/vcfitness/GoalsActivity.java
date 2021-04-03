package com.example.vcfitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DayOfWeek;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class GoalsActivity extends AppCompatActivity {
    BarChart barchart;
    BarData bardata;
    BarDataSet bardataset;
    ArrayList barEntries;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String strUid;
    private FirebaseAuth mAuth;
    ArrayList<String> strMillis = new ArrayList<>();
    ArrayList<String> strWeights = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        mAuth = FirebaseAuth.getInstance();
        strUid = mAuth.getUid();
        LoadProfileInfo();

    }



    private void getEntries()
    {

        barchart = findViewById(R.id.BarChart);

        final ArrayList<String> ArrDay = GetDateFormat();


        //set each day to the X axis of the barchart
        XAxis xAxis = barchart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value >= 0)
                {
                    if(value <= ArrDay.size() - 1)
                    {
                        return ArrDay.get((int) value);
                    }
                    return "";
                }
                return "";
            }
        });


        //add entries to the barchart to populate the data (barchart of weight)
        barEntries = new ArrayList<>();

        barEntries.add(new BarEntry(0, Integer.parseInt(strWeights.get(0))));
        barEntries.add(new BarEntry(1, Integer.parseInt(strWeights.get(1))));
        barEntries.add(new BarEntry(2, Integer.parseInt(strWeights.get(2))));
        barEntries.add(new BarEntry(3, Integer.parseInt(strWeights.get(3))));
        barEntries.add(new BarEntry(4, Integer.parseInt(strWeights.get(4))));
        barEntries.add(new BarEntry(5, Integer.parseInt(strWeights.get(5))));
        barEntries.add(new BarEntry(6, Integer.parseInt(strWeights.get(6))));


        bardataset = new BarDataSet(barEntries, "Daily Weight Changes");
        bardata = new BarData(bardataset);
        barchart.setData(bardata);
        bardataset.setColors(ColorTemplate.JOYFUL_COLORS);
        bardata.setValueTextColor(Color.BLACK);
        bardataset.setValueTextSize(18f);
        barchart.notifyDataSetChanged();
        barchart.invalidate();
    }


    private void LoadProfileInfo()
    {

        //load information of the users profile stored in firebase.
        DocumentReference docRef = db.collection("DailyWeight").document(strUid);

        docRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists())
                            {
                                for (Object o : doc.getData().values()){
                                    strWeights.add(o.toString());
                                }

                                strMillis.addAll(doc.getData().keySet());
                                getEntries();
                                Toast.makeText(GoalsActivity.this, strMillis.get(0), Toast.LENGTH_LONG).show();

                            }
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GoalsActivity.this, "FAILED", Toast.LENGTH_LONG).show();
            }
        });
    }

    //set get all times in milli seconds and add to new array in updated time format
    private ArrayList<String> GetDateFormat(){

        DateFormat format = new SimpleDateFormat("E");
        ArrayList<String> newArrTime = new ArrayList<>();

        Calendar calender = Calendar.getInstance();

        for (String s : strMillis)
        {
            calender.setTimeInMillis(Long.parseLong(s));
            newArrTime.add(format.format(calender.getTime()));
        }


        return newArrTime;

    }

}
