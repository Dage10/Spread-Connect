package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daviddam.clickconnect.databinding.ItemComentariBinding
import models.Comentari
import util.ImageExtension.loadImageOrDefault

class ComentarisAdapter(
    private var comentaris: List<Comentari>,
    private val idUsuariLoguejat: String?,
    private val onEditar: (Comentari) -> Unit,
    private val onEliminar: (Comentari) -> Unit,
    private val onLike: (Comentari) -> Unit = {},
    private val onDislike: (Comentari) -> Unit = {},
    private val onRespostes: (Comentari) -> Unit = {},
    private val onUserClick: (String) -> Unit = {}
): RecyclerView.Adapter<ComentarisAdapter.ComentariViewHolder>() {

    fun updateData(nous: List<Comentari>) {
        comentaris = nous
        notifyDataSetChanged()
    }

    inner class ComentariViewHolder(val binding: ItemComentariBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentariViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemComentariBinding.inflate(inflater, parent, false)
        return ComentariViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComentariViewHolder, posicio: Int) {
        val comentari = comentaris[posicio]
        val esDelMateixUsuari = idUsuariLoguejat == comentari.id_usuari

        holder.binding.apply {
            imgAvatarUser.loadImageOrDefault(comentari.avatar_url, isProfile = true)
            tvUsuari.text = comentari.nom_usuari ?: "Usuari"

            tvData.text = comentari.created_at.take(10)
            if (comentari.created_at.length >= 16) {
                tvHora.text = comentari.created_at.substring(11, 16)
            }

            tvContingut.text = comentari.contingut

            imgComentari.loadImageOrDefault(comentari.imatge_url, isProfile = false)
            imgComentari.visibility = if (comentari.imatge_url.isNullOrEmpty()) View.GONE else View.VISIBLE

            textLikeComptador.text = comentari.likes.toString()
            textDislikeComptador.text = comentari.dislikes.toString()

            if (comentari.reaccioActual == "like") {
                btnLike.setColorFilter(android.graphics.Color.RED)
            } else {
                btnLike.clearColorFilter()
            }

            if (comentari.reaccioActual == "dislike") {
                btnDislike.setColorFilter(android.graphics.Color.BLUE)
            } else {
                btnDislike.clearColorFilter()
            }

            btnEditar.visibility = if (esDelMateixUsuari) View.VISIBLE else View.GONE
            btnEliminar.visibility = if (esDelMateixUsuari) View.VISIBLE else View.GONE

            btnLike.setOnClickListener { onLike(comentari) }
            btnDislike.setOnClickListener { onDislike(comentari) }
            btnEditar.setOnClickListener { onEditar(comentari) }
            btnEliminar.setOnClickListener { onEliminar(comentari) }
            btnComentari.setOnClickListener { onRespostes(comentari) }
            
            imgAvatarUser.setOnClickListener { onUserClick(comentari.id_usuari) }
            tvUsuari.setOnClickListener { onUserClick(comentari.id_usuari) }
        }
    }

    override fun getItemCount(): Int = comentaris.size
}
