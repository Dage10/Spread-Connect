package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daviddam.spreadconnect.R
import com.daviddam.spreadconnect.databinding.ItemHistorialBinding
import models.ActivitatHistorial
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HistorialAdapter : RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    private var historial: List<ActivitatHistorial> = emptyList()

    fun updateData(novaLlista: List<ActivitatHistorial>) {
        historial = novaLlista
        notifyDataSetChanged()
    }

    inner class HistorialViewHolder(val binding: ItemHistorialBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val binding = ItemHistorialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistorialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, posicio: Int) {
        val activitat = historial[posicio]
        val context = holder.itemView.context

        holder.binding.tvTipus.visibility = View.GONE
        holder.binding.tvTitol.visibility = View.GONE
        holder.binding.tvDescripcio.visibility = View.GONE
        holder.binding.tvDataFinal.visibility = View.GONE
        holder.binding.llInteraccions.visibility = View.GONE

        when (activitat.tipus) {
            "total_posts" -> {
                holder.binding.tvTipus.text = context.getString(R.string.total_posts)
                holder.binding.tvTipus.setTextColor(context.getColor(R.color.text_historial))
                holder.binding.tvTitol.text = "${activitat.num_posts}"
                holder.binding.tvTipus.visibility = View.VISIBLE
                holder.binding.tvTitol.visibility = View.VISIBLE

            }
            "total_presentacions" -> {
                holder.binding.tvTipus.text = context.getString(R.string.total_presentacions)
                holder.binding.tvTipus.setTextColor(context.getColor(R.color.text_historial))
                holder.binding.tvTitol.text = "${activitat.num_presentacions}"
                holder.binding.tvTipus.visibility = View.VISIBLE
                holder.binding.tvTitol.visibility = View.VISIBLE
            }
            "comentaris" -> {
                holder.binding.tvTipus.text = context.getString(R.string.comentaris)
                holder.binding.tvTipus.setTextColor(context.getColor(R.color.text_historial))
                holder.binding.tvTitol.text = "${activitat.num_comentaris}"
                holder.binding.tvTipus.visibility = View.VISIBLE
                holder.binding.tvTitol.visibility = View.VISIBLE
            }
            "estadistiques" -> {
                holder.binding.tvTipus.text = context.getString(R.string.estadistiques)
                holder.binding.tvTipus.setTextColor(context.getColor(R.color.text_historial))
                holder.binding.tvTitol.text = "${activitat.num_seguidors} ${context.getString(R.string.seguidors)} · ${activitat.num_seguint} ${context.getString(R.string.seguint)}"
                holder.binding.tvTipus.visibility = View.VISIBLE
                holder.binding.tvTitol.visibility = View.VISIBLE
            }
            "post_mes_interaccions" -> {
                holder.binding.tvTipus.text = context.getString(R.string.postMesInteraccions)
                holder.binding.tvTipus.setTextColor(context.getColor(R.color.text_historial))
                holder.binding.tvTipus.visibility = View.VISIBLE
                holder.binding.tvTitol.text = activitat.titol_post ?: ""
                holder.binding.tvTitol.visibility = if (activitat.titol_post.isNullOrEmpty()) View.GONE else View.VISIBLE
                holder.binding.tvDescripcio.text = activitat.contingut ?: ""
                holder.binding.tvDescripcio.visibility = if (activitat.contingut.isNullOrEmpty()) View.GONE else View.VISIBLE
                holder.binding.llInteraccions.visibility = View.VISIBLE
                holder.binding.tvLikes.text = activitat.num_likes.toString()
                holder.binding.tvDislikes.text = activitat.num_dislikes.toString()
            }
            "ultim_post" -> {
                holder.binding.tvTipus.text = context.getString(R.string.ultim_post)
                holder.binding.tvTipus.setTextColor(context.getColor(R.color.text_historial))
                holder.binding.tvTipus.visibility = View.VISIBLE
                holder.binding.tvTitol.text = activitat.titol_post ?: ""
                holder.binding.tvTitol.visibility = if (activitat.titol_post.isNullOrEmpty()) View.GONE else View.VISIBLE
                holder.binding.tvDescripcio.text = activitat.contingut ?: ""
                holder.binding.tvDescripcio.visibility = if (activitat.contingut.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            "ultim_presentacio" -> {
                holder.binding.tvTipus.text = context.getString(R.string.ultim_presentacio)
                holder.binding.tvTipus.setTextColor(context.getColor(R.color.text_historial))
                holder.binding.tvTipus.visibility = View.VISIBLE
                holder.binding.tvTitol.text = activitat.titol_presentacio ?: ""
                holder.binding.tvTitol.visibility = if (activitat.titol_presentacio.isNullOrEmpty()) View.GONE else View.VISIBLE
                holder.binding.tvDescripcio.text = activitat.contingut ?: ""
                holder.binding.tvDescripcio.visibility = if (activitat.contingut.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            else -> {
                holder.binding.tvTipus.text = activitat.tipus
                holder.binding.tvTipus.visibility = View.VISIBLE
            }
        }

        if (activitat.created_at.isNotEmpty()) {
            holder.binding.tvDataFinal.visibility = View.VISIBLE
            holder.binding.tvDataFinal.text = formatData(activitat.created_at)
        }
    }

    override fun getItemCount(): Int = historial.size

    companion object {
        fun formatData(data: String): String {
            return try {
                val instant = java.time.Instant.parse(data)
                val dataFormat = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                dataFormat.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (_: Exception) {
                data.substringBefore("T")
            }
        }
    }
}