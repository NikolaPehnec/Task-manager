package hr.fer.projekt.taskmanager.view.ui.main

import android.content.Context
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import hr.fer.projekt.taskmanager.model.DatabaseHelper
import hr.fer.projekt.taskmanager.model.Popis


private var popisi = mutableListOf<Popis>()
private var mCurrentFragment: Fragment? = null

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(
    private val context: Context,
    fm: FragmentManager,
) :
    FragmentPagerAdapter(fm) {

    init {
        popisi.clear()
        val dbHelper = DatabaseHelper(context)
        try {
            val cur = dbHelper.VratiPodatkeRaw("SELECT * FROM ${dbHelper.tabPopisZadataka}")
            if (cur != null && cur.count > 0) {
                while (cur.moveToNext()) {
                    val id = cur.getString(cur.getColumnIndexOrThrow(dbHelper.popisId))
                    val naziv = cur.getString(cur.getColumnIndexOrThrow(dbHelper.popisIme))
                    popisi.add(Popis(id, naziv))
                }
            }
        } catch (e: Exception) {
            System.err.print(e.message)
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun getCurrentFragment(): Fragment? {
        return mCurrentFragment
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        if (getCurrentFragment() !== `object`) {
            mCurrentFragment = `object` as Fragment
        }
        super.setPrimaryItem(container, position, `object`)
    }


    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return PlaceholderFragment.newInstance(
            popisi[position].id,
            popisi[position].naziv
        )
    }


    override fun getPageTitle(position: Int): CharSequence? {
        return popisi[position].naziv
    }

    override fun getCount(): Int {
        return popisi.size
    }


}