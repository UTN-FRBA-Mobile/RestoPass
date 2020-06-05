package com.example.restopass.main.ui.reservations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restopass.R
import com.example.restopass.domain.ReservationResponse
import com.example.restopass.login.signin.SignInFragment
import com.example.restopass.main.common.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_membership.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*


class ReservationsFragment : Fragment() {

    val job = Job()
    val coroutineScope = CoroutineScope(job + Dispatchers.Main)

    private lateinit var reservationsAdapter: ReservationsAdapter

    private lateinit var reservationsViewModel : ReservationResponse

    private lateinit var rootView: View;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        reservationsViewModel = ViewModelProvider(requireActivity()).get(ReservationResponse::class.java)

        rootView = inflater.inflate(R.layout.fragment_reservations, container, false)
        return rootView;
    }

    override fun onStart() {
        super.onStart()

        coroutineScope.launch {
            try {
                reservationsViewModel.get()
                reservationsAdapter.list = reservationsViewModel.reservations
                reservationsAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                if(isActive) {
                    Timber.e(e)

                    val titleView: View =
                        layoutInflater.inflate(R.layout.alert_dialog_title, container, false)
                    AlertDialog.getAlertDialog(
                        context,
                        titleView,
                        view
                    ).show()
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState);
        reservationsAdapter = ReservationsAdapter();

        rootView.findViewById<RecyclerView>(R.id.my_recycler_view).apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = reservationsAdapter
        }
    }

}
