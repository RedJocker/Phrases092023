package org.hyperskill.phrases

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.hyperskill.phrases.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val phraseList = mutableListOf<Phrase>(
            Phrase("Just bodies"),
            Phrase("What the hell are you"),
            Phrase("To strive, to seek, to find and not to yield")
        )

        val adapter = PhraseRecyclerAdapter(phraseList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }
}