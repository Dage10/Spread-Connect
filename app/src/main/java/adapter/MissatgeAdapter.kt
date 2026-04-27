package adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.daviddam.clickconnect.R
import com.daviddam.clickconnect.databinding.ItemMissatgeBinding
import models.Missatge
import util.ImageExtension.loadImageOrDefault
import java.time.LocalDate
import java.time.ZoneId

class MissatgeAdapter(
    private var missatges: List<Missatge>,
    private val idUsuariLoguejat: String?
) : RecyclerView.Adapter<MissatgeAdapter.MissatgeViewHolder>() {

    fun updateData(nousMissatges: List<Missatge>) {
        missatges = nousMissatges
        notifyDataSetChanged()
    }

    inner class MissatgeViewHolder(val binding: ItemMissatgeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissatgeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMissatgeBinding.inflate(inflater, parent, false)
        return MissatgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MissatgeViewHolder, posicio: Int) {
        val missatge = missatges[posicio]
        val context = holder.itemView.context

        if (!missatge.imatge_url.isNullOrEmpty()) {
            holder.binding.ivImatge.visibility = android.view.View.VISIBLE
            holder.binding.ivImatge.loadImageOrDefault(missatge.imatge_url, isProfile = false)
            holder.binding.tvMissatge.visibility = if (missatge.contingut.isNullOrEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        } else {
            holder.binding.ivImatge.visibility = android.view.View.GONE
            holder.binding.tvMissatge.visibility = android.view.View.VISIBLE
        }

        holder.binding.tvMissatge.text = missatge.contingut ?: ""

        if (missatge.created_at.isNotEmpty()) {
            val dataStr = missatge.created_at
            val dataPart = dataStr.take(10)
            val horaPart = dataStr.take(16).takeLast(5)
            val avui = LocalDate.now(ZoneId.systemDefault()).toString()
            holder.binding.tvHora.text = if (dataPart == avui) {
                horaPart
            } else {
                dataPart.takeLast(2) + "/" + dataPart.take(7).takeLast(2) + "/" + dataPart.take(4) + " " + horaPart
            }
        }

        val isFromMe = missatge.id_usuari == idUsuariLoguejat

        val params = holder.binding.messageContainer.layoutParams as? android.widget.LinearLayout.LayoutParams
        params?.let {
            it.gravity = if (isFromMe) Gravity.END else Gravity.START
            holder.binding.messageContainer.layoutParams = it
        }

        val backgroundColor = if (isFromMe) {
            ContextCompat.getColor(context, R.color.blue)
        } else {
            ContextCompat.getColor(context, R.color.missatge_rebut_background)
        }
        holder.binding.messageContainer.setBackgroundColor(backgroundColor)

        val textColor = if (isFromMe) {
            ContextCompat.getColor(context, R.color.text_on_item)
        } else {
            ContextCompat.getColor(context, R.color.missatge_rebut_text)
        }
        holder.binding.tvMissatge.setTextColor(textColor)
        holder.binding.tvHora.setTextColor(textColor)
    }

    override fun getItemCount(): Int = missatges.size
}