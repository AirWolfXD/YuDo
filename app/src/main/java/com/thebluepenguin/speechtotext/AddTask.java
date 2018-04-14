package com.thebluepenguin.speechtotext;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ferdousur Rahman Sarker on 3/17/2018.
 */

public class AddTask extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {


    TaskDBHelper mydb;
    DatePickerDialog dpd;
    int startYear = 0, startMonth = 0, startDay = 0;
    String dateFinal;
    String nameFinal;
    Button delete;
    TextView warning;
    ImageView border;

    Intent intent;
    Boolean isUpdate;
    String id;

    Boolean isDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_add_new);

        mydb = new TaskDBHelper(getApplicationContext());
        intent = getIntent();
        isUpdate = intent.getBooleanExtra("isUpdate", false);

        delete = (Button) findViewById(R.id.deleteButton);
        warning = (TextView) findViewById(R.id.warningText);
        border = (ImageView) findViewById(R.id.warningBorder);

        delete.setVisibility(View.INVISIBLE);
        warning.setVisibility(View.INVISIBLE);
        border.setVisibility(View.INVISIBLE);

        dateFinal = todayDateString();
        Date your_date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(your_date);
        startYear = cal.get(Calendar.YEAR);
        startMonth = cal.get(Calendar.MONTH);
        startDay = cal.get(Calendar.DAY_OF_MONTH);

        isDelete = false;

        if (isUpdate) {
            init_update();
        }
    }


    public void init_update() {
        isDelete = false;
        id = intent.getStringExtra("id");
        TextView toolbar_task_add_title = (TextView) findViewById(R.id.toolbar_task_add_title);
        EditText task_name = (EditText) findViewById(R.id.task_name);
        EditText task_date = (EditText) findViewById(R.id.task_date);

        delete.setVisibility(View.VISIBLE);
        warning.setVisibility(View.VISIBLE);
        border.setVisibility(View.VISIBLE);

        toolbar_task_add_title.setText("Update");
        Cursor task = mydb.getDataSpecific(id);
        if (task != null) {
            task.moveToFirst();

            task_name.setText(task.getString(1).toString());
            Calendar cal = Function.Epoch2Calender(task.getString(2).toString());
            startYear = cal.get(Calendar.YEAR);
            startMonth = cal.get(Calendar.MONTH);
            startDay = cal.get(Calendar.DAY_OF_MONTH);
            task_date.setText(Function.Epoch2DateString(task.getString(2).toString(), "dd/MM/yyyy"));

        }
    }

    public void onDelete(View v) {
        isDelete = false;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Delete entry");
        alert.setMessage("Are you sure you want to delete?");
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Delete
                EditText task_name = (EditText) findViewById(R.id.task_name);
                EditText task_date = (EditText) findViewById(R.id.task_date);
                nameFinal = task_name.getText().toString();
                dateFinal = task_date.getText().toString();
                Intent intent = new Intent(AddTask.this, TaskHome.class);
                isDelete = true;
                intent.putExtra("isDelete", isDelete);
                intent.putExtra("task", nameFinal);
                intent.putExtra("date", dateFinal);
                mydb.deleteContact(id);
                startActivity(intent);
                finish();
            }
        });
        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // close dialog
                dialog.cancel();
            }
        });
        alert.show();
    }

    public String todayDateString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd/MM/yyyy", Locale.getDefault());

        return dateFormat.toString();

    }


    public void closeAddTask(View v) {
        isDelete = false;
        Intent intent = new Intent(AddTask.this, TaskHome.class);
        intent.putExtra("isDelete", isDelete);
        startActivity(intent);
        finish();
    }


    public void doneAddTask(View v) {
        isDelete = false;
        Intent intent = new Intent(AddTask.this, TaskHome.class);
        intent.putExtra("isDelete", isDelete);
        int errorStep = 0;
        EditText task_name = (EditText) findViewById(R.id.task_name);
        EditText task_date = (EditText) findViewById(R.id.task_date);
        nameFinal = task_name.getText().toString();
        dateFinal = task_date.getText().toString();


        /* Checking */
        if (nameFinal.trim().length() < 1) {
            errorStep++;
            task_name.setError("Provide a task name.");
        }

        if (dateFinal.trim().length() < 4) {
            errorStep++;
            task_date.setError("Provide a specific date");
        }


        if (errorStep == 0) {
            if (isUpdate) {
                mydb.updateContact(id, nameFinal, dateFinal);
                Toast.makeText(getApplicationContext(), "Task Updated.", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            } else {
                mydb.insertContact(nameFinal, dateFinal);
                Toast.makeText(getApplicationContext(), "Task Added.", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }

            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag("startDatepickerdialog");
        if (dpd != null) dpd.setOnDateSetListener(this);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        startYear = year;
        startMonth = monthOfYear;
        startDay = dayOfMonth;
        int monthAddOne = startMonth + 1;
        String date = (startDay < 10 ? "0" + startDay : "" + startDay) + "/" +
                (monthAddOne < 10 ? "0" + monthAddOne : "" + monthAddOne) + "/" +
                startYear;
        EditText task_date = (EditText) findViewById(R.id.task_date);
        task_date.setText(date);
    }


    public void showStartDatePicker(View v) {
        dpd = DatePickerDialog.newInstance(AddTask.this, startYear, startMonth, startDay);
        dpd.setOnDateSetListener(this);
        dpd.show(getFragmentManager(), "startDatepickerdialog");
    }

}