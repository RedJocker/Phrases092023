package org.hyperskill.phrases

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Phrase(val phraseText:String)

class PhraseRecyclerAdapter( phrases: List<Phrase>) : RecyclerView.Adapter<PhraseViewHolder>() {

    private var phrases = phrases.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhraseViewHolder {
        return PhraseViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_phrase, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PhraseViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.phraseTextView).text =
            phrases[position].phraseText
        holder.itemView.findViewById<TextView>(R.id.deleteTextView).setOnClickListener {
            phrases.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return phrases.size
    }
}

class PhraseViewHolder(phraseView: View) : RecyclerView.ViewHolder(phraseView)