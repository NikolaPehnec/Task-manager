package hr.fer.projekt.taskmanager.view.ui.main

import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import hr.fer.projekt.taskmanager.R
import hr.fer.projekt.taskmanager.adapters.GrupaArrayAdapter
import hr.fer.projekt.taskmanager.adapters.SpinnerItem
import hr.fer.projekt.taskmanager.controllers.*
import hr.fer.projekt.taskmanager.databinding.ZadatakDetailBinding
import hr.fer.projekt.taskmanager.model.DatabaseHelper
import hr.fer.projekt.taskmanager.model.Grupa
import hr.fer.projekt.taskmanager.model.Obavijest
import hr.fer.projekt.taskmanager.view.IView


class ZadatakDetailActivity() : AppCompatActivity(), IView {

    private var _binding: ZadatakDetailBinding? = null
    private var zadatakId: String = ""
    private var popisiList: ArrayList<SpinnerItem> = ArrayList()
    private var grupaList: ArrayList<Grupa> = ArrayList()
    private var obavijestiZadatka: ArrayList<Obavijest> = ArrayList()
    private val popisController: IPopisController = PopisController(this,this)
    private val grupaController: IGrupaController = GrupaController(this,this)
    private val zadatakController: IZadatakController = ZadatakController(this,this)
    private val obavijestController: IObavijestController = ObavijestController(this,this)
    var dbHelper = DatabaseHelper(this)
    var naziv = ""
    var opis = ""
    var popisid = ""
    var grupaId = ""
    var datumVrijeme = ""
    var trajanje = ""
    var dani = ""

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        zadatakId = intent.getStringExtra("zadatak_id").toString()
        _binding = ZadatakDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
        fillPopisList()
        fillGrupaList()
        initPopisAdapter()
        initGrupaAdapter()
        initZadatak()

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.zadatk_detail_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.zadatak_done -> zavrsiZadatak()
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun initPopisAdapter() {
        val popisArrayAdapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(
                this,
                R.layout.custom_simple_spinner_item,
                popisiList as List<Any?>
            )
        popisArrayAdapter.setDropDownViewResource(R.layout.custom_simple_spinner_dropdown_item)
        binding.spinnerPopis.adapter = popisArrayAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initGrupaAdapter() {
        val grupaArrayAdapter: ArrayAdapter<*> =
            GrupaArrayAdapter(this, grupaList)
        binding.spinnerGrupa.adapter = grupaArrayAdapter
    }

    fun initListeners() {
        binding.pilFabZapisi.setOnClickListener { zapisiPodatke() }
        binding.pilObrisi.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Brisanje zadatka")
            builder.setMessage("Brisanjem zadatka će se obrisati sva njegova pojavljivanja i alarmi\n želite li nastaviti?")
            builder.setPositiveButton("Da") { dialog, _ ->
                obrisiZadatak()
            }

            builder.setNegativeButton("Ne") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initZadatak() {
        try {
            val cur = dbHelper.VratiPodatkeRaw(
                "SELECT * FROM ${dbHelper.tabZadatak} WHERE ${dbHelper.zadId}='${zadatakId}'"
            )
            if (cur != null && cur.count > 0) {
                if (cur.moveToNext()) {
                    naziv = cur.getString(cur.getColumnIndexOrThrow(dbHelper.zadNaziv))
                    opis = cur.getString(cur.getColumnIndexOrThrow(dbHelper.zadOpis))
                    popisid = cur.getString(cur.getColumnIndexOrThrow(dbHelper.popisId))
                    grupaId = cur.getString(cur.getColumnIndexOrThrow(dbHelper.grupaId))
                    trajanje = cur.getString(cur.getColumnIndexOrThrow(dbHelper.zadTrajanje))
                    dani = cur.getString(cur.getColumnIndexOrThrow(dbHelper.zadPonavljanjeDani))

                    binding.zadatakEt.setText(naziv)
                    binding.opisEt.setText(opis)
                    binding.spinnerPopis.setSelection(popisiList.indexOfFirst { p -> p.id == popisid })
                    binding.spinnerGrupa.setSelection(grupaList.indexOfFirst { p -> p.id == grupaId })
                }
            }

            var cur2 = dbHelper.VratiPodatkeRaw(
                "SELECT * FROM ${dbHelper.tabObavijest} WHERE ${dbHelper.obavijestIdZadatak}='${zadatakId}' AND ${dbHelper.obavijestNapomena}='prvi'"
            )
            if (cur2 != null && cur2.count > 0) {
                cur2.moveToNext()
                datumVrijeme =
                    cur2.getString(cur2.getColumnIndexOrThrow(dbHelper.obavijestDatumVrijeme))
                binding.btnDatum.text = datumVrijeme
                if (trajanje != "0") {
                    var daniString = ucitajDaneIzIntegera(dani)
                    var _dana = "dana"
                    if (trajanje == "1")
                        _dana = "dan"
                    val tekstPonavljanje = trajanje + " $_dana, danima:\n" + daniString
                    binding.btnPonovi.visibility = View.VISIBLE
                    binding.btnPonovi.text = tekstPonavljanje

                } else {
                    binding.btnPonovi.visibility = View.GONE
                }
            } else {
                binding.btnDatum.visibility = View.GONE
                binding.calendarImage.visibility = View.GONE
                binding.btnPonovi.visibility = View.GONE
                binding.repeatImage.visibility = View.GONE
            }

            cur2 = dbHelper.VratiPodatkeRaw(
                "SELECT * FROM ${dbHelper.tabObavijest} WHERE ${dbHelper.obavijestIdZadatak}='${zadatakId}'"
            )

            if (cur2 != null && cur2.count > 0) {
                while (cur2.moveToNext()) {
                    val napomena =
                        cur2.getString(cur2.getColumnIndexOrThrow(dbHelper.obavijestNapomena))
                    val alarmId = cur2.getInt(cur2.getColumnIndexOrThrow(dbHelper.obavijestIdAlarm))
                    val datumVrijeme =
                        cur2.getString(cur2.getColumnIndexOrThrow(dbHelper.obavijestDatumVrijeme))
                    val obavijest = Obavijest("", datumVrijeme, napomena)
                    obavijest.alarmId = alarmId
                    obavijestiZadatka.add(obavijest)
                }
            }

            println("OBAVIJESTI")
            println(obavijestiZadatka)
        } catch (e: Exception) {
            System.err.print(e.message)
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun ucitajDaneIzIntegera(dani: String): String {
        var str = ""
        for (dan in dani.split(",")) {
            when (dan) {
                "1" -> str += " Pon"
                "2" -> str += " Uto"
                "3" -> str += " Sri"
                "4" -> str += " Čet"
                "5" -> str += " Pet"
                "6" -> str += " Sub"
                "7" -> str += " Ned"
            }
        }
        return str
    }

    private fun fillPopisList() {
        val sviPopisi = popisController.getAllPopisi()
        for (popis in sviPopisi) {
            popisiList.add(SpinnerItem(popis.id, popis.naziv, ""))
        }
    }

    private fun fillGrupaList() {
        grupaList.addAll(grupaController.getAllGrupe())
        grupaList.add(Grupa("", "Nema grupu", "#ffffff", "X"))
        grupaList.sortWith(Comparator { t, t2 -> t.prioritet!!.compareTo(t2.prioritet!!) })
    }


    fun zapisiPodatke() {
        if (binding.zadatakEt.text.toString() == "") {
            Toast.makeText(this, "Ispunite naziv zadatka", Toast.LENGTH_LONG).show()
            return
        }
        try {
            dbHelper.UpdatePodatke(
                dbHelper.tabZadatak,
                arrayOf(dbHelper.zadNaziv, dbHelper.zadOpis, dbHelper.zadPopis, dbHelper.zadGrupa),
                arrayOf(
                    binding.zadatakEt.text.toString(),
                    binding.opisEt.text.toString(),
                    (binding.spinnerPopis.selectedItem as SpinnerItem).id,
                    (binding.spinnerGrupa.selectedItem as Grupa).id
                ),
                dbHelper.zadId + "=?",
                arrayOf(zadatakId)
            )
        } catch (e: Exception) {
            System.err.print(e.message)
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }

        setResult(Activity.RESULT_OK)
        finish()
    }

    fun zavrsiZadatak() {
        try {
            dbHelper.UpdatePodatke(
                dbHelper.tabZadatak,
                arrayOf(dbHelper.zadStatus),
                arrayOf(
                    "2"
                ),
                dbHelper.zadId + "=?",
                arrayOf(zadatakId)
            )
        } catch (e: Exception) {
            System.err.print(e.message)
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
        Toast.makeText(this, "Zadatak $naziv završen", Toast.LENGTH_LONG).show()

        setResult(Activity.RESULT_OK)
        finish()
    }

    fun obrisiZadatak() {
        zadatakController.deleteZadatakByID(zadatakId)
        obavijestController.deleteObavijestByZadatakID(zadatakId)

        for (obavijest in obavijestiZadatka) {
            obavijest.cancelAlarm(this)
        }

        Toast.makeText(this, "Zadatak $naziv obrisan", Toast.LENGTH_LONG).show()

        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onDatabaseError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDataSaveSuccess() {
    }

}