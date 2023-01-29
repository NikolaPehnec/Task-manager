package hr.fer.projekt.taskmanager.view.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import hr.fer.projekt.taskmanager.adapters.ZadaciArrayAdapter
import hr.fer.projekt.taskmanager.controllers.*
import hr.fer.projekt.taskmanager.databinding.FragmentMainBinding
import hr.fer.projekt.taskmanager.model.DatabaseHelper
import hr.fer.projekt.taskmanager.model.Grupa
import hr.fer.projekt.taskmanager.model.Popis
import hr.fer.projekt.taskmanager.model.Zadatak
import hr.fer.projekt.taskmanager.view.IView
import java.text.SimpleDateFormat
import java.util.*

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment() : Fragment(),
    ZadaciArrayAdapter.ZadatakItemClickListener,
IView{

    private var zadaciArrayAdapter: ZadaciArrayAdapter? = null
    private var dovrseniZadaciArrayAdapter: ZadaciArrayAdapter? = null
    private var _binding: FragmentMainBinding? = null
    private var popisFragmenta: Popis? = null
    private var zadaciList: ArrayList<Zadatak> = ArrayList()
    private var dovrseniZadaciList: ArrayList<Zadatak> = ArrayList()
    private var zadatakController: IZadatakController? = null
    private var obavijestController: IObavijestController? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        popisFragmenta =
            Popis(
                requireArguments().getString(ARG_POPIS_ID)!!,
                requireArguments().getString(ARG_POPIS_NAZIV)!!
            )

        setHasOptionsMenu(true)

        zadatakController = ZadatakController(requireContext(),this)
        obavijestController = ObavijestController(requireContext(),this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.prikaziZavrsene.setOnClickListener {
            binding.zadaciRecyclerDovrseni.visibility = View.VISIBLE
            binding.prikaziZavrsene2.visibility = View.VISIBLE
            binding.prikaziZavrsene.visibility = View.GONE
        }

        binding.prikaziZavrsene2.setOnClickListener {
            binding.zadaciRecyclerDovrseni.visibility = View.GONE
            binding.prikaziZavrsene.visibility = View.VISIBLE
            binding.prikaziZavrsene2.visibility = View.GONE
        }

        initZadaciRecycler()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initZadaciRecycler() {
        val dbHelper = DatabaseHelper(requireContext())
        try {
            var zadaciHashSet = hashSetOf<Zadatak>()
            zadaciList.clear()
            dovrseniZadaciList.clear()
            val cur = dbHelper.VratiPodatkeRaw(
                "SELECT A.idZadatak as zadatakIdOrig, * FROM ${dbHelper.tabZadatak} A INNER JOIN ${dbHelper.tabPopisZadataka} B ON " +
                        "A.${dbHelper.zadPopis}=B.${dbHelper.popisId} INNER JOIN ${dbHelper.tabStatus} C ON " +
                        "A.${dbHelper.zadStatus}=C.${dbHelper.statusId} LEFT OUTER JOIN ${dbHelper.tabGrupaZadataka} D ON " +
                        "A.${dbHelper.zadGrupa}=D.${dbHelper.grupaId} LEFT OUTER JOIN ${dbHelper.tabObavijest} E ON " +
                        "A.${dbHelper.zadId}=E.${dbHelper.obavijestIdZadatak}" +
                        " WHERE A.${dbHelper.zadPopis}='${popisFragmenta!!.id}'"
            )
            if (cur != null && cur.count > 0) {
                while (cur.moveToNext()) {
                    val id = cur.getString(cur.getColumnIndexOrThrow("zadatakIdOrig"))
                    val naziv = cur.getString(cur.getColumnIndexOrThrow(dbHelper.zadNaziv))
                    val opis = cur.getString(cur.getColumnIndexOrThrow(dbHelper.zadOpis))
                    val status = cur.getString(cur.getColumnIndexOrThrow(dbHelper.statusOznaka))
                    val popis = cur.getString(cur.getColumnIndexOrThrow(dbHelper.popisIme))
                    val grupaId = cur.getString(cur.getColumnIndexOrThrow(dbHelper.grupaId))
                    val ponavljajuciZadatak =
                        cur.getString(cur.getColumnIndexOrThrow(dbHelper.zadPonavljajuci))
                    val obavijestId =
                        cur.getString(cur.getColumnIndexOrThrow(dbHelper.obavijestIdZadatak))
                    val datumVrijemeZadatak =
                        cur.getString(cur.getColumnIndexOrThrow(dbHelper.zadDatumVrijeme))
                    val datumVrijemeObavijest =
                        cur.getString(cur.getColumnIndexOrThrow(dbHelper.obavijestDatumVrijeme))
                    val obavijestNapomena =
                        cur.getString(cur.getColumnIndexOrThrow(dbHelper.obavijestNapomena))
                    var zadatak: Zadatak? = null
                    var grupa: Grupa? = null

                    if (grupaId != null) {
                        val grupaNaziv = cur.getString(cur.getColumnIndexOrThrow(dbHelper.grupaIme))
                        val grupaPrioritet =
                            cur.getString(cur.getColumnIndexOrThrow(dbHelper.grupaPrioritet))
                        val grupaBoja = cur.getString(cur.getColumnIndexOrThrow(dbHelper.grupaBoja))
                        grupa = Grupa(grupaId, grupaNaziv, grupaBoja, grupaPrioritet)
                    }

                    zadatak =
                        Zadatak(
                            id,
                            naziv,
                            opis,
                            status,
                            popis,
                            "",
                            datumVrijemeZadatak,
                            grupa,
                            ponavljajuciZadatak == "1"
                        )


                    if (obavijestId == null) {
                        if (status == "Završen") {
                            dovrseniZadaciList.add(zadatak)
                        } else {
                            zadaciHashSet.add(zadatak)
                        }
                    } else if (datumVrijemeObavijest.split(" ")[0] == SimpleDateFormat("MM/dd/yyyy").format(
                            Calendar.getInstance().time
                        )
                    ) { //Zadatak se ponavlja i danasnji je datum
                        if (obavijestNapomena == "gotovo") {
                            dovrseniZadaciList.add(zadatak)
                        } else {
                            zadaciHashSet.add(zadatak)
                        }
                        //ako je inicijalni datum u proslosti, buduca pojavljivanja pokazati samo ako je
                    } else if (obavijestNapomena == "prvi") {
                        zadaciHashSet.add(zadatak)
                    } else if (obavijestNapomena == "gotovo") {
                        dovrseniZadaciList.add(zadatak)
                    }


                }
            }

            zadaciList.addAll(zadaciHashSet)
        } catch (e: Exception) {
            System.err.print(e.message)
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }

        if (zadaciList.size > 0 || dovrseniZadaciList.size > 0) {
            binding.nemaZadatkaTV.visibility = View.GONE
        } else {
            binding.nemaZadatkaTV.visibility = View.VISIBLE
        }

        if (dovrseniZadaciList.size == 0) {
            binding.view.visibility = View.GONE
            binding.view2.visibility = View.GONE
            binding.nemaZadatkaTV2.visibility = View.GONE
            binding.prikaziZavrsene.visibility = View.GONE
            binding.prikaziZavrsene2.visibility = View.GONE
        } else {
            binding.view.visibility = View.VISIBLE
            if (binding.prikaziZavrsene2.visibility == View.GONE)
                binding.prikaziZavrsene.visibility = View.VISIBLE
            binding.view2.visibility = View.VISIBLE
            binding.nemaZadatkaTV2.visibility = View.VISIBLE
            binding.nemaZadatkaTV2.text = "Dovršeno (" + dovrseniZadaciList.size + ")"
        }

        val zadaciRecycler = binding.zadaciRecycler
        val dovrseniZadaciRecycler = binding.zadaciRecyclerDovrseni
        zadaciRecycler.layoutManager = LinearLayoutManager(requireContext())
        dovrseniZadaciRecycler.layoutManager = LinearLayoutManager(requireContext())

        zadaciArrayAdapter =
            ZadaciArrayAdapter(
                requireContext(),
                zadaciList,
                false,
                this
            )

        dovrseniZadaciArrayAdapter =
            ZadaciArrayAdapter(
                requireContext(),
                dovrseniZadaciList,
                true,
                this
            )

        zadaciRecycler.adapter = zadaciArrayAdapter
        dovrseniZadaciRecycler.adapter = dovrseniZadaciArrayAdapter
    }

    fun getCurrentPopisID(): String {
        return popisFragmenta!!.id
    }

    companion object {
        private const val ARG_POPIS_ID = "popis_id"
        private const val ARG_POPIS_NAZIV = "popis_naziv"

        @JvmStatic
        fun newInstance(
            popisId: String,
            popisNaziv: String,
        ): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_POPIS_ID, popisId)
                    putString(ARG_POPIS_NAZIV, popisNaziv)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onZadatakZavrsenClick(zadatak: Zadatak?, position: Int) {
        zadatakController!!.updateZadatakStatusZavrsen(zadatak!!)
        if (zadatak.datum != null) {
            obavijestController!!.updateObavijestStatusGotovo(zadatak)
        }

        zadaciArrayAdapter!!.removeItem(position)
        dovrseniZadaciArrayAdapter!!.addItem(zadatak)
        azurirajUI()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureDoneClick(zadatak: Zadatak?, position: Int) {

        zadatakController!!.updateZadatakStatusNeizvrsen(zadatak!!)
        if (zadatak.datum != null) {
            obavijestController!!.updateObavijestStatusNijeGotovo(zadatak)
        }

        dovrseniZadaciArrayAdapter!!.removeItem(position)
        zadaciArrayAdapter!!.addItem(zadatak)
        azurirajUI()
    }

    private fun azurirajUI() {
        if (dovrseniZadaciArrayAdapter!!.listSize() == 0) {
            binding.view.visibility = View.GONE
            binding.view2.visibility = View.GONE
            binding.nemaZadatkaTV2.visibility = View.GONE
            binding.prikaziZavrsene.visibility = View.GONE
            binding.prikaziZavrsene2.visibility = View.GONE
        } else {
            binding.view.visibility = View.VISIBLE
            if (binding.prikaziZavrsene2.visibility == View.GONE) {
                binding.prikaziZavrsene.visibility = View.VISIBLE
                binding.zadaciRecyclerDovrseni.visibility = View.GONE
            }
            binding.view2.visibility = View.VISIBLE
            binding.nemaZadatkaTV2.visibility = View.VISIBLE
            binding.nemaZadatkaTV2.text =
                "Dovršeno (" + dovrseniZadaciArrayAdapter!!.listSize() + ")"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(zadatak: Zadatak?) {

        /* (requireActivity() as MainActivity).ucitajDetailZadatak(zadatak!!.id)
         val intent = Intent(requireActivity(), ZadatakDetailActivity::class.java)*/

        val intent = Intent(requireContext(), ZadatakDetailActivity::class.java)
        intent.putExtra("zadatak_id", zadatak!!.id)
        resultLauncher.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                initZadaciRecycler()
            }
        }

    interface NewPopisInterface {
        fun onNewPopisClicked()
        fun onPopisDeleted()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        initZadaciRecycler()

    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        //Toast.makeText(requireContext(), "2", Toast.LENGTH_LONG).show()
    }

    override fun onDatabaseError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDataSaveSuccess() {
    }
}