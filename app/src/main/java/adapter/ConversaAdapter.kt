package adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daviddam.clickconnect.databinding.ItemConversaBinding
import models.Conversa
import util.ImageExtension.loadImageOrDefault
import java.time.LocalDate
import java.time.ZoneId

class ConversaAdapter(
    private var converses: List<Conversa>,
    private val idUsuariLoguejat: String?,
    private val onClick: (Conversa) -> Unit
) : RecyclerView.Adapter<ConversaAdapter.ConversaViewHolder>() {

    fun updateData(novesConverses: List<Conversa>) {
        converses = novesConverses
        notifyDataSetChanged()
    }

    inner class ConversaViewHolder(val binding: ItemConversaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemConversaBinding.inflate(inflater, parent, false)
        return ConversaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversaViewHolder, posicio: Int) {
        val conversa = converses[posicio]
        val altreUsuari = conversa.usuaris?.firstOrNull {
            it.id_usuari != idUsuariLoguejat
        } ?: conversa.usuaris?.firstOrNull()

        holder.binding.tvNomUsuari.text = altreUsuari?.nom_usuari ?: ""
        holder.binding.imgAvatarUser.loadImageOrDefault(altreUsuari?.avatar_url, isProfile = true)

        val ultimMissatge = conversa.ultim_missatge
        holder.binding.tvUltimMissatge.text = ultimMissatge?.contingut ?: ""

        if (ultimMissatge?.imatge_url != null) {
            holder.binding.tvUltimMissatge.text = "Imatge"
        }

        if (ultimMissatge?.created_at?.isNotEmpty() == true) {
            val dataStr = ultimMissatge.created_at
            val dataPart = dataStr.take(10)
            val horaPart = if (dataStr.length >= 16) dataStr.take(16).takeLast(5) else ""
            val avui = LocalDate.now(ZoneId.systemDefault()).toString()
            holder.binding.tvData.text = if (dataPart == avui) {
                horaPart
            } else {
                dataPart.takeLast(2) + "/" + dataPart.take(7).takeLast(2) + "/" + dataPart.take(4)
            }
        }

        holder.binding.root.setOnClickListener { onClick(conversa) }
    }

    override fun getItemCount(): Int = converses.size
}