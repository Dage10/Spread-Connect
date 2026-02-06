package adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daviddam.clickconnect.databinding.ItemAreaBinding
import models.Area
import androidx.core.content.ContextCompat
import com.daviddam.clickconnect.R

class AreesAdapter(
    private var llistaArees: List<Area>,
    private val onClick: (Area, View) -> Unit
) : RecyclerView.Adapter<AreesAdapter.AreaViewHolder>() {

    private var seleccionatId: String? = null

    fun updateData(novaLlista: List<Area>) {
        llistaArees = novaLlista
        notifyDataSetChanged()
    }

    fun setSelected(id: String?) {
        seleccionatId = id
        notifyDataSetChanged()
    }

    inner class AreaViewHolder(val binding: ItemAreaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAreaBinding.inflate(inflater, parent, false)
        return AreaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AreaViewHolder, posicio: Int) {
        val area = llistaArees[posicio]
        val context = holder.itemView.context
        holder.binding.btnArea.text = area.nom

        val isSelected = area.id == seleccionatId

        holder.binding.btnArea.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.item_background))

        val strokeColor = if (isSelected) {
            Color.parseColor("#14B8A6")
        } else {
            Color.TRANSPARENT
        }

        holder.binding.btnArea.strokeColor = ColorStateList.valueOf(strokeColor)
        holder.binding.btnArea.strokeWidth = if (isSelected) 10 else 0
        holder.binding.btnArea.setTextColor(ContextCompat.getColor(context, R.color.text_on_item))

        holder.itemView.post {
            val recyclerView = holder.itemView.parent as? RecyclerView
            if (recyclerView != null) {
                val availableWidth = recyclerView.width - recyclerView.paddingLeft - recyclerView.paddingRight
                val itemWidth = availableWidth / 3

                val params = holder.itemView.layoutParams
                if (params.width != itemWidth) {
                    params.width = itemWidth
                    holder.itemView.layoutParams = params
                }
            }
        }

        holder.binding.btnArea.setOnClickListener {
            onClick(area, it)
        }
    }

    override fun getItemCount(): Int = llistaArees.size
}
