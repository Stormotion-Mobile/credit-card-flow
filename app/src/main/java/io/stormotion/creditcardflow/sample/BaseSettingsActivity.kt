package io.stormotion.creditcardflow.sample

import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import kotlinx.android.synthetic.main.settings_base_activity.*
import kotlinx.android.synthetic.main.settings_base_activity.view.*

abstract class BaseSettingsActivity : AppCompatActivity() {

    abstract val menuRes: Int?
    abstract val toolbarTitleRes: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_base_activity)

        toolbar.toolbar_header.text = resources.getString(toolbarTitleRes)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        with(window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = ContextCompat.getColor(context, R.color.settingsColor)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return menuRes?.let {
            menuInflater.inflate(it, menu)
            true
        } ?: false
    }

    override fun onOptionsItemSelected(item: MenuItem?) =
            when (item?.itemId) {
                android.R.id.home -> {
                    onBackPressed()
                    true
                }
                else -> false
            }

}