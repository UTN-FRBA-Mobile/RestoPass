package com.example.restopass.main.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.restopass.R
import com.example.restopass.databinding.ViewSettingItemBinding
import kotlinx.android.synthetic.main.view_setting_item.view.*
import java.lang.ClassCastException

class SettingsAdapter(private val settings: List<Setting>, private val listener: SettingAdapterListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEWTYPE_CATEGORY = 1
    private val VIEWTYPE_CATEGORY_BUTTON = 2
    private val VIEWTYPE_BUTTON = 3

    //EL ORDEN DE LLAMADAS ES ESTE
    override fun getItemCount() = settings.size

    override fun getItemViewType(position: Int): Int {
        return if(settings[position].typeButton in ButtonSettingType.values()){
            return if (settings[position].typeButton == ButtonSettingType.SESSION) VIEWTYPE_BUTTON
            else  VIEWTYPE_CATEGORY_BUTTON
        }
        else VIEWTYPE_CATEGORY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEWTYPE_CATEGORY -> CategoryViewHolder.from(parent)
            VIEWTYPE_CATEGORY_BUTTON -> CategoryButtonViewHolder.from(parent)
            VIEWTYPE_BUTTON -> ButtonViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val setting = settings[position]

        holder.itemView.apply {
            settingTitle?.setText(setting.title)

            setting.image?.let {
               settingImage.setImageResource(it)
            }

            setting.typeButton?.let {
                setOnClickListener {
                    listener.onClick(setting.typeButton)
                }
            }
        }
    }

    class CategoryButtonViewHolder private constructor(val binding: ViewSettingItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): CategoryButtonViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ViewSettingItemBinding.inflate(layoutInflater, parent, false)

                return CategoryButtonViewHolder(binding)
            }
        }
    }

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): CategoryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.view_category_item, parent, false)
                return CategoryViewHolder(view)
            }
        }
    }

    class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): ButtonViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.view_button_item, parent, false)
                return ButtonViewHolder(view)
            }
        }
    }

}

interface SettingAdapterListener {
    fun onClick(type: ButtonSettingType)
}

