package com.example.restopass

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.login_activity.*
import timber.log.Timber

class LoginActivity : AppCompatActivity(), LoginFragment.OnFragmentInteractionListener, SignInFragment.OnFragmentInteractionListener, SignUpFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate started")
        setContentView(R.layout.login_activity)
        setSupportActionBar(toolbar)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LoginFragment())
                .commit()
        }
    }

    override fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun changeToolbar(fragmentName: String) {
        toolbar.title = fragmentName
    }
}
