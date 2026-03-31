package com.tarsislimadev.traderapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.tarsislimadev.traderapp.databinding.FragmentFirstBinding
import androidx.lifecycle.lifecycleScope
import com.tarsislimadev.traderapp.repository.BinanceRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.widget.Toast




/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private val binanceRepository = BinanceRepository()

    private val projectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
            val intent = Intent(requireContext(), ScreenRecorderService::class.java).apply {
                putExtra("RESULT_CODE", result.resultCode)
                putExtra("RESULT_DATA", result.data)
                putExtra("STREAM_URL", "rtmp://localhost/live/test") // Placeholder URL
            }
            ContextCompat.startForegroundService(requireContext(), intent)
            binding.buttonStart.visibility = View.GONE
            binding.buttonStop.visibility = View.VISIBLE
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start Ticker
        binanceRepository.startTickerWebSocket("btcusdt")

        viewLifecycleOwner.lifecycleScope.launch {
            binanceRepository.tickerFlow.collectLatest { ticker ->
                ticker?.let {
                    binding.textSymbol.text = it.symbol
                    binding.textPrice.text = it.currentPrice
                }
            }
        }

        binding.buttonStart.setOnClickListener {
            val projectionManager = requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
        }


        binding.buttonStop.setOnClickListener {
            requireContext().stopService(Intent(requireContext(), ScreenRecorderService::class.java))
            binding.buttonStop.visibility = View.GONE
            binding.buttonStart.visibility = View.VISIBLE
        }

        binding.buttonBuy.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val response = binanceRepository.createMarketBuyOrder(
                    "BTCUSDT",
                    "0.001",
                    "YOUR_API_KEY",
                    "YOUR_SECRET_KEY"
                )
                if (response != null) {
                    Toast.makeText(requireContext(), "Order Placed!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Order Failed (Check Keys)", Toast.LENGTH_SHORT).show()
                }
            }
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        binanceRepository.stopTickerWebSocket()
        _binding = null
    }

}