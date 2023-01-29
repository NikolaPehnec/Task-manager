package hr.fer.projekt.taskmanager.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import hr.fer.projekt.taskmanager.R
import hr.fer.projekt.taskmanager.adapters.ZadaciArrayAdapter
import hr.fer.projekt.taskmanager.controllers.IObavijestController
import hr.fer.projekt.taskmanager.controllers.IZadatakController
import hr.fer.projekt.taskmanager.controllers.ZadatakController
import hr.fer.projekt.taskmanager.databinding.ActivityCalendarBinding
import hr.fer.projekt.taskmanager.model.DatabaseHelper
import hr.fer.projekt.taskmanager.model.Grupa
import hr.fer.projekt.taskmanager.model.Zadatak
import hr.fer.projekt.taskmanager.utility.Constants
import hr.fer.projekt.taskmanager.view.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class CalendarActivitiy : AppCompatActivity(),
    ZadaciArrayAdapter.ZadatakItemClickListener, IView {

    private lateinit var binding: ActivityCalendarBinding
    lateinit var toggle: ActionBarDrawerToggle
    private var googleSignInClient: GoogleSignInClient? = null
    private lateinit var pref: SharedPreferences
    private var dbHelper = DatabaseHelper(this)
    private var zadaciList: ArrayList<Zadatak> = ArrayList()
    private var zadaciArrayAdapter: ZadaciArrayAdapter? = null
    private val zadatakController: IZadatakController = ZadatakController(this, this)
    private var obavijestController: IObavijestController? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)

        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pref = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.apply {
            toggle = ActionBarDrawerToggle(
                this@CalendarActivitiy,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_closed,
            )
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            navView.setCheckedItem(R.id.calendar_item)
            navView.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.home_item -> {
                        startActivity(
                            Intent(this@CalendarActivitiy, MainActivity::class.java)
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
            Glide.with(this@CalendarActivitiy)
                .load(
                    photoUrl
                )
                .into(headerLayout.findViewById<ImageView>(R.id.user_image))
        }

        initZadaciRecycler()
        postaviListenere()
        postaviDanasnjiDatum()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            true
        }
        return super.onOptionsItemSelected(item)
    }

    fun postaviDanasnjiDatum() {
        binding.calendarView.setDateSelected(Calendar.getInstance().time, true)
        azurirajZadatke(SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance().time))
    }

    fun postaviListenere() {
        binding.calendarView.setOnDateChangedListener(object : OnDateSelectedListener {
            override fun onDateSelected(
                widget: MaterialCalendarView,
                date: CalendarDay,
                selected: Boolean
            ) {
                azurirajZadatke(SimpleDateFormat("MM/dd/yyyy").format(date.date))
            }
        })

        binding.calendarView.addDecorator(CalendarDayDecorator(this@CalendarActivitiy))
    }


    fun azurirajZadatke(datum: String) {
        try {
            //Ponavljajuce zadatke prikazat ali se ne mogu završiti, osim ako su danas
            //Ako su ponavljajuci danas - izbrisat tu obavijest u bazi i sustavu
            //Ako nije ponavljajuci postavit azuriranost na zavrseno
            zadaciList.clear()
            val cur = dbHelper.VratiPodatkeRaw(
                "SELECT * FROM ${dbHelper.tabObavijest} A INNER JOIN ${dbHelper.tabZadatak} B ON " +
                        "A.${dbHelper.obavijestIdZadatak} = B.${dbHelper.zadId} INNER JOIN ${dbHelper.tabPopisZadataka} C ON " +
                        "B.${dbHelper.zadPopis} = C.${dbHelper.popisId} INNER JOIN ${dbHelper.tabStatus} D ON " +
                        "B.${dbHelper.zadStatus}= D.${dbHelper.statusId} LEFT OUTER JOIN ${dbHelper.tabGrupaZadataka} E ON " +
                        "B.${dbHelper.zadGrupa} = E.${dbHelper.grupaId}" +
                        " WHERE substr(${dbHelper.obavijestDatumVrijeme}, 0, instr(${dbHelper.obavijestDatumVrijeme},' '))='" + datum + "'" +
                        " AND A.${dbHelper.obavijestNapomena} <> 'gotovo'"
            )
            if (cur != null && cur.count > 0) {
                while (cur.moveToNext()) {
                    val id = cur.getString(cur.getColumnIndexOrThrow(dbHelper.zadId))
                    val naziv = cur.getString(cur.getColumnIndexOrThrow(dbHelper.zadNaziv))
                    val opis = cur.getString(cur.getColumnIndexOrThrow(dbHelper.zadOpis))
                    val status = cur.getString(cur.getColumnIndexOrThrow(dbHelper.statusOznaka))
                    val napomena =
                        cur.getString(cur.getColumnIndexOrThrow(dbHelper.obavijestNapomena))
                    val popis = cur.getString(cur.getColumnIndexOrThrow(dbHelper.popisIme))
                    val grupaId = cur.getString(cur.getColumnIndexOrThrow(dbHelper.grupaId))
                    var datumVrijeme =
                        cur.getString(cur.getColumnIndexOrThrow(dbHelper.obavijestDatumVrijeme))
                    var zadatak: Zadatak? = null
                    var grupa: Grupa? = null

                    if (grupaId != null) {
                        val grupaNaziv = cur.getString(cur.getColumnIndexOrThrow(dbHelper.grupaIme))
                        val grupaPrioritet =
                            cur.getString(cur.getColumnIndexOrThrow(dbHelper.grupaPrioritet))
                        val grupaBoja = cur.getString(cur.getColumnIndexOrThrow(dbHelper.grupaBoja))
                        grupa = Grupa(grupaId, grupaNaziv, grupaBoja, grupaPrioritet)
                    }

                    zadatak = Zadatak(
                        id,
                        naziv,
                        opis,
                        status,
                        popis,
                        "",
                        datumVrijeme,
                        grupa
                    )

                    zadatak.napomena = napomena

                    val danasnjiDatum = Calendar.getInstance().time
                    val datumZadatka = SimpleDateFormat("MM/dd/yyyy").parse(datum)

                    //Zadatak koji nije ponavljanje pocetnog ili je ponavljanje koje je proslo moze se završiti
                    zadatak.aktualniZadatak =
                        napomena == "prvi" || datumZadatka.before(danasnjiDatum) || datumZadatka.equals(
                            danasnjiDatum
                        )

                    if (status == "Završen" && napomena == "prvi") {
                        //Ako je zadatak dovršen u prvom datumu ne prikazuje se
                        //dovrseniZadaciList.add(zadatak)
                    } else {
                        //Ako nije završen ili je njegov ponavljac prikazuje se,
                        //ponavljanje zadatka se prikazuje razlicitom bojom
                        zadaciList.add(zadatak)
                    }
                }
            }
        } catch (e: Exception) {
            System.err.print(e.message)
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }

        zadaciArrayAdapter!!.refillZadaci(zadaciList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initZadaciRecycler() {
        val zadaciRecycler = binding.zadaciRecycler
        zadaciRecycler.layoutManager = LinearLayoutManager(this)

        zadaciArrayAdapter =
            ZadaciArrayAdapter(
                this,
                zadaciList,
                false,
                this
            )

        zadaciRecycler.adapter = zadaciArrayAdapter
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

    override fun onItemClick(zadatak: Zadatak?) {
    }

    override fun onZadatakZavrsenClick(zadatak: Zadatak?, position: Int) {
        if (zadatak!!.napomena == "prvi") {
            zadatakController.updateZadatakStatusZavrsen(zadatak)
        }
        //zadaciList.clear()
        obavijestController!!.updateObavijestStatusGotovo(zadatak)
        zadaciArrayAdapter!!.removeItem(position)
    }

    override fun onPictureDoneClick(zadatak: Zadatak?, position: Int) {
    }

    override fun onDatabaseError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDataSaveSuccess() {
    }

}