package hr.fer.projekt.taskmanager.view

import android.content.Context
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import hr.fer.projekt.taskmanager.R
import hr.fer.projekt.taskmanager.model.DatabaseHelper


class CalendarDayDecorator(context: Context) : DayViewDecorator {
    private val dates: HashSet<CalendarDay> = hashSetOf()
    private val contex: Context = context


    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.setSelectionDrawable(
            AppCompatResources.getDrawable(
                contex,
                R.drawable.date_selected
            )!!
        )
    }

    init {
        try {
            //Ponavljajuce zadatke prikazat ali se ne mogu završiti, osim ako su danas
            //Ako su ponavljajuci danas - izbrisat tu obavijest u bazi i sustavu
            //Ako nije ponavljajuci postavit azuriranost na zavrseno
            val dbHelper = DatabaseHelper(context)
            val cur = dbHelper.VratiPodatkeRaw(
                "SELECT substr(${dbHelper.obavijestDatumVrijeme}, 0, instr(${dbHelper.obavijestDatumVrijeme},' ')) as vrijeme " +
                        " FROM ${dbHelper.tabObavijest} A INNER JOIN ${dbHelper.tabZadatak} B ON " +
                        "A.${dbHelper.obavijestIdZadatak} = B.${dbHelper.zadId}"+
                " AND A.${dbHelper.obavijestNapomena} <> 'gotovo'"
            )
            if (cur != null && cur.count > 0) {
                while (cur.moveToNext()) {
                    val vrijeme = cur.getString(cur.getColumnIndexOrThrow("vrijeme"))
                    val month = Integer.parseInt(vrijeme.split("/")[0])-1
                    val day = Integer.parseInt(vrijeme.split("/")[1])
                    val year = Integer.parseInt(vrijeme.split("/")[2])
                    dates.add(CalendarDay(year, month, day))
                }
            }
        } catch (e: Exception) {
            System.err.print(e.message)
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }
}