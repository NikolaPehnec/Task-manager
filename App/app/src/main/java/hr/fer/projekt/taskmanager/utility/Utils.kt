package hr.fer.projekt.taskmanager.utility

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TimePicker
import hr.fer.projekt.taskmanager.R
import hr.fer.projekt.taskmanager.view.ui.main.MainActivity
import java.text.SimpleDateFormat
import java.util.*

object Utils {
}

fun EditText.transformIntoDatePicker(context: Context, format: String, maxDate: Date? = null) {
    isFocusableInTouchMode = false
    isClickable = true
    isFocusable = false

    val myCalendar = Calendar.getInstance()
    val datePickerOnDataSetListener =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val sdf = SimpleDateFormat(format, Locale.UK)
            setText("Početni datum: " + sdf.format(myCalendar.time))
        }

    setOnClickListener {
        DatePickerDialog(
            context, datePickerOnDataSetListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
            myCalendar.get(Calendar.DAY_OF_MONTH)
        ).run {
            maxDate?.time?.also { datePicker.maxDate = it }
            show()
        }
    }
}

fun EditText.transformIntoTimePicker(context: Context) {
    isFocusableInTouchMode = false
    isClickable = true
    isFocusable = false

    setOnClickListener {
        val dialog2 = Dialog(context)
        dialog2.setContentView(R.layout.time_picker_dialog)
        dialog2.setCanceledOnTouchOutside(true)
        val picker = dialog2.findViewById<View>(R.id.timePicker1) as TimePicker
        picker.setIs24HourView(true)
        dialog2.findViewById<View>(R.id.btnZapisi).setOnClickListener {
            var hourStr = ""
            var minuteStr = ""

            hourStr = if (picker.hour < 10)
                "0" + picker.hour.toString()
            else
                picker.hour.toString()

            minuteStr = if (picker.minute < 10)
                "0" + picker.minute.toString()
            else
                picker.minute.toString()

            setText("Vrijeme: $hourStr:$minuteStr")
            dialog2.dismiss()
        }

        dialog2.setOnDismissListener {
            if (context is MainActivity) {
                postDelayed(Runnable {
                    context.hideKeyboard()

                }, 50)
            }
        }

        dialog2.show()
    }
}