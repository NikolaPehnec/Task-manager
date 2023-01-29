package hr.fer.projekt.taskmanager.view.ui.main

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import hr.fer.projekt.taskmanager.R
import hr.fer.projekt.taskmanager.adapters.GrupaArrayAdapter
import hr.fer.projekt.taskmanager.adapters.SpinnerItem
import hr.fer.projekt.taskmanager.adapters.VremenaArrayAdapter
import hr.fer.projekt.taskmanager.controllers.*
import hr.fer.projekt.taskmanager.databinding.ActivityMainBinding
import hr.fer.projekt.taskmanager.model.*
import hr.fer.projekt.taskmanager.utility.Constants
import hr.fer.projekt.taskmanager.utility.transformIntoDatePicker
import hr.fer.projekt.taskmanager.utility.transformIntoTimePicker
import hr.fer.projekt.taskmanager.view.CalendarActivitiy
import hr.fer.projekt.taskmanager.view.IView
import hr.fer.projekt.taskmanager.view.LoginActivity
import kotlinx.android.synthetic.main.activity_main.*
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity(), PlaceholderFragment.NewPopisInterface,
    VremenaArrayAdapter.ItemClickListener, IView {

    private lateinit var binding: ActivityMainBinding
    lateinit var toggle: ActionBarDrawerToggle
    private var googleSignInClient: GoogleSignInClient? = null
    private lateinit var pref: SharedPreferences
    private var dbHelper = DatabaseHelper(this)
    private var dialog: Dialog? = null
    private var vremenaList: ArrayList<String> = ArrayList()
    private var vremenaArrayAdapter: VremenaArrayAdapter? = null

    private var popisiList: ArrayList<SpinnerItem> = ArrayList()
    private var grupaList: ArrayList<Grupa> = ArrayList()
    private var odabraniDaniUzimanja = mutableListOf<Int>()
    private var sectionsPagerAdapter: SectionsPagerAdapter? = null
    private val zadatakController: IZadatakController = ZadatakController(this,this)
    private val obavijestController: IObavijestController = ObavijestController(this,this)
    private val popisController: IPopisController = PopisController(this,this)
    private val grupaController: IGrupaController = GrupaController(this,this)
    var novaBoja = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        binding.pilFabZapisi.setOnClickListener { otvoriDialogZadatka() }
        binding.apply {
            toggle = ActionBarDrawerToggle(
                this@MainActivity,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_closed,
            )
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            navView.setCheckedItem(R.id.home_item)
            navView.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.calendar_item -> {
                        startActivity(
                            Intent(this@MainActivity, CalendarActivitiy::class.java)
                        )
                    }
                    R.id.logout_item -> {
                        signOut()
                    }
                    R.id.upload_item -> {
                        navView.menu.findItem(R.id.upload_item).isCheckable = false
                        navView.menu.findItem(R.id.upload_item).isEnabled = false
                        uploadNaFirebase()
                    }
                }
                true
            }

            val headerLayout: View = navView.inflateHeaderView(R.layout.header_layout)
            headerLayout.findViewById<TextView>(R.id.header_ime_korisnik).text =
                pref.getString(Constants.USER_NAME, "Ivan Horvat")
            var photoUrl =
                "http://www.seekpng.com/png/detail/110-1100707_person-avatar-placeholder.png"
            Glide.with(this@MainActivity)
                .load(
                    photoUrl
                )
                .into(headerLayout.findViewById<ImageView>(R.id.user_image))
        }

        fillPopisList()
        fillGrupaList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.fragment_options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.izbrisi_popis -> pokaziDialogBrisanja()
            R.id.novi_popis -> onNewPopisClicked()
            R.id.grupe -> pokaziDialogGrupa()
        }

        if (toggle.onOptionsItemSelected(item)) {
            true
        }
        return super.onOptionsItemSelected(item)
    }

    fun TabLayout.setTabLastChildWidthAsWrapContent() {
        for (i in 1..this.childCount) {
            val layout =
                (this.getChildAt(0) as LinearLayout).getChildAt(i - 1) as LinearLayout
            val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = 0f
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layout.layoutParams = layoutParams
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun otvoriDialogZadatka() {
        dialog = Dialog(this)
        dialog!!.setContentView(R.layout.novi_zadatak_dialog)
        dialog!!.setTitle("Novi zadatak")
        dialog!!.setCanceledOnTouchOutside(false)
        val trenutniPopisId = popisiList[tabs.selectedTabPosition].id

        val btnOpis = dialog!!.findViewById<View>(R.id.btnOpis) as Button
        val btnVrijeme = dialog!!.findViewById<View>(R.id.btnVrijeme) as Button
        val btnGrupa = dialog!!.findViewById<View>(R.id.btnGrupa) as Button
        val opisTIL = dialog!!.findViewById<View>(R.id.opisTIL) as TextInputLayout
        val datumVrijemeSV = dialog!!.findViewById<View>(R.id.datumVrijemeSV) as ScrollView
        val grupaLL = dialog!!.findViewById<View>(R.id.grupaLinearLayout) as LinearLayout
        val layoutObavijesti = dialog!!.findViewById<View>(R.id.layoutObavijesti)
        val switchPonovi = dialog!!.findViewById<View>(R.id.switchPonovi) as SwitchCompat
        val spinnerPopis = dialog!!.findViewById<View>(R.id.spinnerPopis) as AppCompatSpinner
        val spinnerGrupa = dialog!!.findViewById<View>(R.id.spinnerGrupa) as AppCompatSpinner
        val zadatakEt = dialog!!.findViewById<View>(R.id.zadatak_et) as EditText
        val opisEt = dialog!!.findViewById<View>(R.id.opisEt) as EditText
        dialog!!.findViewById<View>(R.id.zadatak_et).requestFocus()
        showKeyboard()

        val grupaArrayAdapter: ArrayAdapter<*> =
            GrupaArrayAdapter(this, grupaList)
        spinnerGrupa.adapter = grupaArrayAdapter
        val popisArrayAdapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(
                this,
                R.layout.custom_simple_spinner_item,
                popisiList as List<Any?>
            )
        popisArrayAdapter.setDropDownViewResource(R.layout.custom_simple_spinner_dropdown_item)
        spinnerPopis.adapter = popisArrayAdapter
        spinnerPopis.setSelection(popisiList.indexOfFirst { p -> p.id == trenutniPopisId })
        napuniListuVremena()

        dialog!!.setOnKeyListener { arg0, arg1, arg2 ->
            if (arg1 == KeyEvent.KEYCODE_BACK) {
                dialog!!.dismiss()
            }
            false
        }
        btnOpis.setOnClickListener {
            if (opisTIL.visibility == View.GONE) {
                opisTIL.visibility = View.VISIBLE
                dialog!!.findViewById<View>(R.id.opisEt).requestFocus()
            } else {
                opisTIL.visibility = View.GONE
                dialog!!.findViewById<View>(R.id.zadatak_et).requestFocus()
            }
        }
        btnVrijeme.setOnClickListener {
            if (datumVrijemeSV.visibility == View.GONE) {
                datumVrijemeSV.visibility = View.VISIBLE
            } else {
                datumVrijemeSV.visibility = View.GONE
            }
        }
        btnGrupa.setOnClickListener {
            if (grupaLL.visibility == View.GONE) {
                grupaLL.visibility = View.VISIBLE
            } else {
                grupaLL.visibility = View.GONE
            }
        }
        switchPonovi.setOnCheckedChangeListener { compoundButton, checked ->
            if (checked) {
                layoutObavijesti.visibility = View.VISIBLE
            } else {
                layoutObavijesti.visibility = View.GONE
            }
        }
        zadatakEt.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                zapisiNoviZadatak()
                return@OnKeyListener true
            }
            false
        })
        opisEt.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                zapisiNoviZadatak()
                return@OnKeyListener true
            }
            false
        })


        val spinnerVremena =
            dialog!!.findViewById<View>(R.id.spinnerVremena) as Spinner
        spinnerVremena.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                napuniListuVremena()
            }
        }

        val radioBtnNeprekidno =
            dialog!!.findViewById<View>(R.id.neprekidno_rb) as RadioButton
        val radioBtnOdredeno =
            dialog!!.findViewById<View>(R.id.odredeno_rb) as RadioButton

        radioBtnNeprekidno.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                radioBtnOdredeno.text = "Određeni broj dana: "
            }
        }
        radioBtnOdredeno.setOnClickListener {
            ucitajBrojDanaDialog()
        }

        val radioBtnSvakiDan =
            dialog!!.findViewById<View>(R.id.svaki_dan_rb) as RadioButton
        val radioBtnSpecDani =
            dialog!!.findViewById<View>(R.id.odredeni_dani_rb) as RadioButton
        radioBtnSvakiDan.setOnCheckedChangeListener { compoundButton, b ->
            if (b) radioBtnSpecDani.text = "Određeni dani: "
        }
        radioBtnSpecDani.setOnCheckedChangeListener { compoundButton, b ->
            if (b) ucitajSpecDaniDialog()
        }
        val pocetniDatum =
            dialog!!.findViewById<View>(R.id.pocetni_datum_et) as EditText
        pocetniDatum.transformIntoDatePicker(this, "MM/dd/yyyy")
        val vrijeme =
            dialog!!.findViewById<View>(R.id.pocetno_vrijeme_et) as EditText
        vrijeme.transformIntoTimePicker(this)


        val btnZapisi =
            dialog!!.findViewById<View>(R.id.btnZapisi) as Button

        btnZapisi.setOnClickListener {
            zapisiNoviZadatak()
        }

        napuniListuVremena()

        //   dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialog!!.show()
    }

    private fun napuniListuVremena() {
        vremenaList = ArrayList()
        val spinnerObavijesti =
            dialog!!.findViewById<View>(R.id.spinnerVremena) as Spinner
        val kol = spinnerObavijesti.selectedItemPosition

        vremenaList.addAll(mapaVremena[kol + 1]!!.toList())
        val vremenaRecycler = dialog!!.findViewById<RecyclerView>(R.id.recyclerVremena)
        vremenaRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        vremenaArrayAdapter = VremenaArrayAdapter(this, vremenaList, this)
        vremenaRecycler.adapter = vremenaArrayAdapter
    }

    private fun ucitajBrojDanaDialog() {
        val dialog3 = Dialog(this)
        dialog3.setContentView(R.layout.numofday_picker_dialog)
        dialog3.setCanceledOnTouchOutside(true)

        val picker = dialog3.findViewById<View>(R.id.numPicker) as NumberPicker
        picker.minValue = 1
        picker.maxValue = 100

        val btnZapisi = dialog3.findViewById<View>(R.id.btnZapisi) as Button
        btnZapisi.setOnClickListener {
            val value = picker.value
            dialog3.dismiss()
            val radioBtnOdredeno =
                dialog!!.findViewById<RecyclerView>(R.id.odredeno_rb) as RadioButton
            radioBtnOdredeno.text = "Određeni broj dana: " + value.toString()
        }

        //dialog2.setTitle("Novi lijek")
        dialog3.show()
    }

    private fun ucitajSpecDaniDialog() {
        val dialog4 = Dialog(this)
        dialog4.setContentView(R.layout.spec_dani_dialog)
        dialog4.setCanceledOnTouchOutside(false)

        val btnZapisi = dialog4.findViewById<View>(R.id.btnZapisi) as Button
        btnZapisi.setOnClickListener {
            val odabraniDani = ucitajOdabraneDane(dialog4)
            odabraniDaniUzimanja = ucitajOdabraneDaneInt(dialog4)

            if (odabraniDani == "") {
                MaterialAlertDialogBuilder(this)
                    .setMessage("Odaberite bar jedan dan!")
                    .setPositiveButton("OK") { dialog, which ->
                        // Respond to positive button press
                    }
                    .show()
            } else {
                dialog4.dismiss()
                val radioBtnSpecDani =
                    dialog!!.findViewById<RecyclerView>(R.id.odredeni_dani_rb) as RadioButton
                radioBtnSpecDani.text = "Određeni dani: " + odabraniDani
            }
        }

        //dialog2.setTitle("Novi lijek")
        dialog4.show()
    }

    private fun uploadNaFirebase() {
        binding.loginPB.visibility = View.VISIBLE
        val auth = Firebase.auth
        val user = auth.currentUser

        val inFileName = getDatabasePath("baza TM").toString();
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        val dbRef = storageRef.child("databases/${user!!.uid}/baza TM")
        var file = Uri.fromFile(File(inFileName))
        val uploadTask = dbRef.putFile(file)

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            navView.menu.findItem(R.id.upload_item).isCheckable = true
            navView.menu.findItem(R.id.upload_item).isEnabled = true
            binding.loginPB.visibility = View.GONE
            binding.drawerLayout.closeDrawers()

            Toast.makeText(this, "Greška kod uploada podataka", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
            navView.menu.findItem(R.id.upload_item).isCheckable = true
            navView.menu.findItem(R.id.upload_item).isEnabled = true

            binding.loginPB.visibility = View.GONE
            binding.drawerLayout.closeDrawers()

            Toast.makeText(this, "Uspješan upload na cloud!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ucitajOdabraneDane(dialog: Dialog): String {
        val pon = dialog.findViewById<View>(R.id.pon) as CheckBox
        val uto = dialog.findViewById<View>(R.id.uto) as CheckBox
        val sri = dialog.findViewById<View>(R.id.sri) as CheckBox
        val cet = dialog.findViewById<View>(R.id.cet) as CheckBox
        val pet = dialog.findViewById<View>(R.id.pet) as CheckBox
        val sub = dialog.findViewById<View>(R.id.sub) as CheckBox
        val ned = dialog.findViewById<View>(R.id.ned) as CheckBox
        var str = ""
        if (pon.isChecked) str += " Pon"
        if (uto.isChecked) str += " Uto"
        if (sri.isChecked) str += " Sri"
        if (cet.isChecked) str += " Čet"
        if (pet.isChecked) str += " Pet"
        if (sub.isChecked) str += " Sub"
        if (ned.isChecked) str += " Ned"
        return str
    }

    private fun ucitajOdabraneDaneInt(dialog: Dialog): MutableList<Int> {
        val pon = dialog.findViewById<View>(R.id.pon) as CheckBox
        val uto = dialog.findViewById<View>(R.id.uto) as CheckBox
        val sri = dialog.findViewById<View>(R.id.sri) as CheckBox
        val cet = dialog.findViewById<View>(R.id.cet) as CheckBox
        val pet = dialog.findViewById<View>(R.id.pet) as CheckBox
        val sub = dialog.findViewById<View>(R.id.sub) as CheckBox
        val ned = dialog.findViewById<View>(R.id.ned) as CheckBox
        var dani = mutableListOf<Int>()
        if (pon.isChecked) dani.add(1)
        if (uto.isChecked) dani.add(2)
        if (sri.isChecked) dani.add(3)
        if (cet.isChecked) dani.add(4)
        if (pet.isChecked) dani.add(5)
        if (sub.isChecked) dani.add(6)
        if (ned.isChecked) dani.add(7)
        return dani
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun zapisiNoviZadatak() {
        //zapisaneObavijesti = ArrayList()
        val opisZadatka = (dialog!!.findViewById<View>(R.id.opisEt) as EditText).text.toString()
        val datumVrijemeSV = dialog!!.findViewById<View>(R.id.datumVrijemeSV) as ScrollView
        val switchPonovi = dialog!!.findViewById<View>(R.id.switchPonovi) as SwitchCompat
        val spinnerPopis = dialog!!.findViewById<View>(R.id.spinnerPopis) as AppCompatSpinner
        val spinnerGrupa = dialog!!.findViewById<View>(R.id.spinnerGrupa) as AppCompatSpinner
        val grupaLL = dialog!!.findViewById<View>(R.id.grupaLinearLayout) as LinearLayout

        var trajanjeZadatka = 365
        var pocetniDatumVrijednost = ""
        var pocetniDatum = ""
        var pocetnoVrijeme = ""
        var pocetnoVrijemeVrijednost = ""
        val nazivZadatka =
            (dialog!!.findViewById<View>(R.id.zadatak_et) as EditText).text.toString()
        val popisId = (spinnerPopis.selectedItem as SpinnerItem).id
        var grupaId = ""
        var ponavljajuci = "0"

        if (nazivZadatka == "") {
            MaterialAlertDialogBuilder(this)
                .setMessage("Unesite naziv zadatka!")
                .setPositiveButton("OK") { dialog, which ->
                }
                .show()
        } else if (datumVrijemeSV.visibility == View.VISIBLE &&
            (dialog!!.findViewById<View>(R.id.pocetni_datum_et) as EditText).text.toString() == "Datum: "
        ) {
            MaterialAlertDialogBuilder(this)
                .setMessage("Unesite datum zadataka!")
                .setPositiveButton("OK") { dialog, which ->
                }
                .show()
        } else {
            if (grupaLL.visibility == View.VISIBLE)
                grupaId = (spinnerGrupa.selectedItem as Grupa).id

            if (datumVrijemeSV.visibility == View.VISIBLE) {
                var listaObavijesti = arrayListOf<Obavijest>()

                pocetniDatum =
                    (dialog!!.findViewById<View>(R.id.pocetni_datum_et) as EditText).text.toString()
                pocetnoVrijeme =
                    (dialog!!.findViewById<View>(R.id.pocetno_vrijeme_et) as EditText).text.toString()

                pocetniDatumVrijednost = pocetniDatum.split(":")[1].trimStart()
                if (pocetnoVrijeme != "Vrijeme: ") {
                    pocetnoVrijemeVrijednost = pocetnoVrijeme.split("eme: ")[1].trimStart()
                } else {
                    pocetnoVrijemeVrijednost = "00:00"
                }

                listaObavijesti.add(
                    Obavijest(
                        null,
                        pocetniDatumVrijednost + " " + pocetnoVrijemeVrijednost,
                        "prvi"
                    )
                )

                if (switchPonovi.isChecked) {
                    ponavljajuci = "1"
                    val odredeniBrojDana =
                        dialog!!.findViewById<View>(R.id.odredeno_rb) as RadioButton
                    val svakiDanUzimanja =
                        dialog!!.findViewById<View>(R.id.svaki_dan_rb) as RadioButton
                    if (odredeniBrojDana.isChecked)
                        trajanjeZadatka =
                            Integer.parseInt(odredeniBrojDana.text.split(":")[1].trimStart())

                    if (svakiDanUzimanja.isChecked) {
                        odabraniDaniUzimanja.clear()
                        odabraniDaniUzimanja.addAll(listOf(1, 2, 3, 4, 5, 6, 7))
                    }

                    // for (vrijeme in vremenaList) {
                    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
                    var pocetniLD = LocalDate.parse(pocetniDatumVrijednost, formatter)
                    pocetniLD = pocetniLD.plusDays(1)

                    //var itrDate: LocalDate = LocalDate.now()
                    var _trajanjeZadatka = trajanjeZadatka
                    while (_trajanjeZadatka > 0) {
                        //Ako je dan u tjednu u kojem se odabralo ponavljanje
                        if (odabraniDaniUzimanja.contains(pocetniLD.dayOfWeek.value)) {
                            _trajanjeZadatka--
                            //novaObavijest.idLijek = id
                            var month = ""
                            if (pocetniLD.monthValue < 10)
                                month = "0" + pocetniLD.monthValue.toString()
                            else month = pocetniLD.monthValue.toString()
                            var day = ""
                            if (pocetniLD.dayOfMonth < 10)
                                day = "0" + pocetniLD.dayOfMonth.toString()
                            else day = pocetniLD.dayOfMonth.toString()

                            val datumVrijeme =
                                month + "/" + day + "/" + pocetniLD.year.toString() + " " + pocetnoVrijemeVrijednost

                            listaObavijesti.add(Obavijest(null, datumVrijeme, ""))
                        }

                        pocetniLD = pocetniLD.plusDays(1)
                    }
                    // }
                } else {
                    trajanjeZadatka = 0
                }

                zapisiObavijestiZadatka(listaObavijesti, nazivZadatka, opisZadatka)
            }

            val pocetniDatumVrijeme =
                (pocetniDatumVrijednost + " " + pocetnoVrijemeVrijednost).trim()
            zapisiZadatakUBazu(
                nazivZadatka, opisZadatka, popisId, grupaId, pocetniDatumVrijeme,
                ponavljajuci, trajanjeZadatka.toString(), odabraniDaniUzimanja.joinToString(",")
            )
            osvjeziZadatkeSvihPopisa()

            val inputMethodManager =
                getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(dialog!!.currentFocus!!.windowToken, 0)
            dialog!!.dismiss()
        }
    }

    private fun zapisiZadatakUBazu(
        nazivZadatka: String,
        opisZadatka: String,
        popisId: String,
        grupaId: String,
        datumVrijeme: String,
        ponavljajuci: String,
        trajanje: String,
        daniPonavljanja: String
    ) {
        zadatakController.onZapisiZadatak(
            Zadatak(
                "0", nazivZadatka, opisZadatka, "1", popisId, trajanje,
                datumVrijeme, Grupa(grupaId), ponavljajuci == "1", daniPonavljanja = daniPonavljanja
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun zapisiObavijestiZadatka(
        obavijesti: List<Obavijest>,
        nazivZadatka: String,
        opisZadatka: String
    ) {
        var idZadatak = ""
        try {
            val cur =
                dbHelper.VratiPodatkeRaw("SELECT IFNULL(MAX(+" + dbHelper.zadId + "), 0) as max_id FROM ${dbHelper.tabZadatak}")
            if (cur != null && cur.count > 0) {
                if (cur.moveToNext()) {
                    val cur2 =
                        dbHelper.VratiPodatkeRaw("SELECT IFNULL(MAX(+" + dbHelper.obavijestIdAlarm + "), 0) as max_id_alarm FROM ${dbHelper.tabObavijest}")
                    cur2!!.moveToNext();
                    val maxId = cur.getString(cur.getColumnIndexOrThrow("max_id"))
                    val maxIdAlarm = cur2.getString(cur2.getColumnIndexOrThrow("max_id_alarm"))
                    idZadatak = (Integer.parseInt(maxId) + 1).toString()
                    var idAlarm = Integer.parseInt(maxIdAlarm) + 1

                    for (obavijest in obavijesti) {
                        idAlarm += 1
                        obavijest.idZadatak = idZadatak
                        obavijest.alarmId = idAlarm
                        obavijest.naslov = nazivZadatka
                        obavijest.kreirano = System.currentTimeMillis()
                        obavijest.idAlarm = idAlarm.toString()

                        obavijestController.onZapisiObavijest(obavijest)

                        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")
                        if (LocalDateTime.parse(obavijest.datumVrijeme, formatter)
                                .isAfter(LocalDateTime.now())
                        ) {
                            obavijest.schedule(this, obavijest.datumVrijeme!!)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            System.err.print(e.message)
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pokaziDialogGrupa() {
        val dialog4 = Dialog(this)
        dialog4.setContentView(R.layout.pregled_grupa_dialog)

        val btnOdustani = dialog4.findViewById<View>(R.id.btnOdustani) as Button
        val btnNovaGrupa = dialog4.findViewById<View>(R.id.btnDodajNovuGrupu) as Button
        val listaGrupa = dialog4.findViewById<View>(R.id.grupeListView) as ListView
        val grupaArrayAdapter: ArrayAdapter<*> =
            GrupaArrayAdapter(this, grupaList)
        listaGrupa.adapter = grupaArrayAdapter

        listaGrupa.setOnItemClickListener { adapterView, view, position, l ->
            pokaziDetailDialogGrupa(grupaList.get(position), dialog4, false)
        }

        btnOdustani.setOnClickListener {
            dialog4.dismiss()
        }

        btnNovaGrupa.setOnClickListener {
            pokaziDetailDialogGrupa(grupaList.get(grupaList.size - 1), dialog4, true)
        }
        dialog4.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pokaziDetailDialogGrupa(grupa: Grupa, dialog: Dialog, novaGrupa: Boolean) {
        val dialog4 = Dialog(this)
        dialog4.setContentView(R.layout.pregled_grupa_detalj_dialog)

        val btnOdustani = dialog4.findViewById<View>(R.id.btnOdustani) as Button
        val btnZapisi = dialog4.findViewById<View>(R.id.btnZapisi) as Button
        val grupaNaziv = dialog4.findViewById<View>(R.id.grupaNaziv) as EditText
        val grupaPrioritet = dialog4.findViewById<View>(R.id.grupaPrioritet) as TextView
        val grupaBoja = dialog4.findViewById<View>(R.id.grupaBoja) as ImageView

        if (novaGrupa) {
            grupaBoja.setBackgroundColor(Color.parseColor("#0277BD"))
            grupaPrioritet.setText((Integer.parseInt(grupa.prioritet) + 1).toString())
        } else {
            grupaNaziv.setText(grupa.naziv)
            grupaPrioritet.setText(grupa.prioritet)
        }

        dialog4.show()

        grupaBoja.setOnClickListener {
            pokaziColorDialog(grupa, dialog4)
        }

        btnOdustani.setOnClickListener {
            dialog4.dismiss()
        }

        btnZapisi.setOnClickListener {
            if (grupaNaziv.text.toString() == "") {
                Toast.makeText(this, "Unesite naziv grupe", Toast.LENGTH_LONG).show()
            } else {
                //Promjena grupe, pa treba refresh liste u roditelj dialogu
                if (novaGrupa) {
                    zapisiGrupu(grupaPrioritet.text.toString(), grupaNaziv.text.toString())
                } else {
                    updateGrupu(grupa, grupaNaziv.text.toString())
                }
                dialog4.dismiss()
                dialog.dismiss()
                fillGrupaList()
                pokaziDialogGrupa()
                osvjeziZadatkeSvihPopisa()
                novaBoja = 0
            }
        }
    }

    private fun pokaziColorDialog(grupa: Grupa, dialogGrupe: Dialog) {
        val dialog = AmbilWarnaDialog(
            this,
            Color.parseColor(grupa.boja!!),
            object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                    novaBoja = 0
                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    novaBoja = color
                    val grupaBoja = dialogGrupe.findViewById<View>(R.id.grupaBoja) as ImageView
                    grupaBoja.setBackgroundColor(novaBoja)
                }
            })

        dialog.show();
    }


    private fun fillPopisList() {
        val sviPopisi = popisController.getAllPopisi()
        for (popis in sviPopisi) {
            popisiList.add(SpinnerItem(popis.id, popis.naziv,""))
        }
    }

    private fun fillGrupaList() {
        grupaList.addAll(grupaController.getAllGrupe())
    }

    private fun signOut() {
        googleSignInClient!!.signOut().addOnCompleteListener {
            if (it.isSuccessful) {
                with(pref.edit()) {
                    this.putInt(Constants.ROLE_ID, 0)
                    this.putString(Constants.USER_NAME, "")
                    this.putString(Constants.USER_PHOTO_URL, "")
                    this.apply()
                }
                uploadNaFirebase()

                startActivity(
                    Intent(this, LoginActivity::class.java)
                )
            } else {
                Toast.makeText(this, "Greška kod odjave!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNewPopisClicked() {
        // if (binding.tabs.selectedTabPosition == binding.tabs.childCount - 1) {
        val dialog3 = Dialog(this)
        dialog3.setContentView(R.layout.new_popis_dialog)
        dialog3.setCanceledOnTouchOutside(false)
        dialog3.setTitle("Novi popis")
        val et = (dialog3.findViewById<View>(R.id.popis_naziv_et) as EditText)

        val btnZapisi = dialog3.findViewById<View>(R.id.btnZapisi) as Button
        val btnOdustani = dialog3.findViewById<View>(R.id.btnOdustani) as Button
        btnOdustani.setOnClickListener {
            dialog3.dismiss()
        }
        btnZapisi.setOnClickListener {
            val text = et.text.toString()
            zapisiNoviPopis(text)
            dialog3.dismiss()
            tabs.selectTab(tabs.getTabAt(tabs.tabCount - 1))
        }

        dialog3.show()
        dialog3.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog3.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun zapisiNoviPopis(nazivPopis: String) {
        if (nazivPopis == "") {
            Toast.makeText(this@MainActivity, "Unesite naziv popisa", Toast.LENGTH_SHORT)
                .show()
            return
        } else {

            popisController.onZapisiPopis(Popis("", nazivPopis))
            osvjeziSvePopise()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun osvjeziZadatkeSvihPopisa() {
        //(sectionsPagerAdapter!!.getCurrentFragment() as PlaceholderFragment).initZadaciRecycler()

        for (fragment in supportFragmentManager.fragments) {
            if (fragment is PlaceholderFragment) {
                fragment.initZadaciRecycler()
            }
        }
    }

    override fun onPopisDeleted() {
        osvjeziSvePopise()
    }

    fun osvjeziSvePopise() {
        fillPopisList()
        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        tabs.setTabLastChildWidthAsWrapContent()
    }


    fun pokaziDialogBrisanja() {
        val adb = AlertDialog.Builder(this)
        adb.setTitle("Brisanje popisa")
        adb.setMessage("Zajedno sa popisom izbrisat će se svi zadaci popisa, želite li nastaviti?")

        adb.setPositiveButton(
            "Ne"
        ) { dialog, id -> dialog.dismiss() }
        adb.setNegativeButton(
            "Da, obriši"
        ) { dialog, id ->
            dialog.dismiss()
            izbrisiPopis((sectionsPagerAdapter!!.getCurrentFragment() as PlaceholderFragment).getCurrentPopisID())
        }
        adb.show()
    }

    fun izbrisiPopis(popisId: String) {
        if (popisId == "") {
            Toast.makeText(this, "Nemoguće brisanje", Toast.LENGTH_LONG).show()
            return
        }
        try {
            popisController.deletePopisById(popisId)
            zadatakController.deleteZadatakByPopisID(popisId)
            osvjeziSvePopise()
        } catch (e: Exception) {
            System.err.print(e.message)
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }


    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Izlazak")
        builder.setMessage("Želite li izaći iz aplikacije?")
        builder.setPositiveButton("Da") { dialog, _ ->
            dialog.dismiss()
            finishAffinity();
        }

        builder.setNegativeButton("Ne") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun updateGrupu(grupa: Grupa, noviNaziv: String) {
        var grupaBoja = grupa.boja
        if (novaBoja != 0) {
            grupaBoja = String.format("#%06X", 0xFFFFFF and novaBoja)
        }

        grupaController.onUpdateGrupa(Grupa(grupa.id, noviNaziv, grupaBoja, ""))
    }

    private fun zapisiGrupu(prioritet: String, naziv: String) {
        var grupaBoja = "#0277BD"
        if (novaBoja != 0) {
            grupaBoja = String.format("#%06X", 0xFFFFFF and novaBoja)
        }

        grupaController.onZapisiGrupa(Grupa("", naziv, grupaBoja, prioritet))
    }

    override fun onItemClick(vrijeme: String?, position: Int) {
        val dialog2 = Dialog(this)
        dialog2.setContentView(R.layout.time_picker_dialog)
        dialog2.setCanceledOnTouchOutside(true)

        val picker = dialog2.findViewById<View>(R.id.timePicker1) as TimePicker
        picker.setIs24HourView(true)
        picker.hour = Integer.parseInt(vrijeme!!.split(":")[0])
        picker.minute = Integer.parseInt(vrijeme.split(":")[1])

        val btnZapisi = dialog2.findViewById<View>(R.id.btnZapisi) as Button
        btnZapisi.setOnClickListener {
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

            vremenaArrayAdapter!!.changeTimeAtIndex("$hourStr:$minuteStr", position)
            dialog2.dismiss()
        }

        dialog2.show()
    }

    override fun onDatabaseError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDataSaveSuccess() {
        //
    }

}