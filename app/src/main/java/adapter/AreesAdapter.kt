package adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.daviddam.spreadconnect.R
import com.daviddam.spreadconnect.databinding.ItemAreaBinding
import models.Area

class AreesAdapter(
    private var llistaArees: List<Area>,
    private val onClick: (Area, android.view.View) -> Unit
) : RecyclerView.Adapter<AreesAdapter.AreaViewHolder>() {

    private var seleccionatId: String? = null
    private var ampleItem = 0

    fun updateData(novaLlista: List<Area>) {
        llistaArees = novaLlista
        notifyDataSetChanged()
    }

    fun setSelected(id: String?) {
        seleccionatId = id
        notifyDataSetChanged()
    }

    fun actualitzarAmpleDisponible(ampleRv: Int) {
        if (ampleRv <= 0 || llistaArees.isEmpty()) return
        val nou = ampleRv / llistaArees.size
        if (nou != ampleItem) {
            ampleItem = nou
            notifyDataSetChanged()
        }
    }

    inner class AreaViewHolder(val binding: ItemAreaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaViewHolder {
        val binding = ItemAreaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.layoutParams = RecyclerView.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
        return AreaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AreaViewHolder, posicio: Int) {
        val area = llistaArees[posicio]
        val context = holder.itemView.context
        holder.binding.btnArea.text = area.nom

        val isSelected = area.id == seleccionatId
        holder.binding.btnArea.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.item_background))
        holder.binding.btnArea.strokeColor = ColorStateList.valueOf(
            if (isSelected) Color.parseColor("#14B8A6") else Color.TRANSPARENT
        )
        holder.binding.btnArea.strokeWidth = if (isSelected) 4 else 0
        holder.binding.btnArea.setTextColor(ContextCompat.getColor(context, R.color.text_on_item))

        if (ampleItem > 0) {
            holder.itemView.layoutParams.width = ampleItem
        }

        holder.binding.btnArea.setOnClickListener { onClick(area, it) }
    }

    override fun getItemCount(): Int = llistaArees.size
}
