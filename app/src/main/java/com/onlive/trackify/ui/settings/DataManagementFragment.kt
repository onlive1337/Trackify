package com.onlive.trackify.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.onlive.trackify.databinding.FragmentDataManagementBinding
import com.onlive.trackify.utils.DataExportImportManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataManagementFragment : Fragment() {

    private var _binding: FragmentDataManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var dataManager: DataExportImportManager

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                exportData(uri)
            }
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                importData(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataManagementBinding.inflate(inflater, container, false)
        dataManager = DataExportImportManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonExport.setOnClickListener {
            startExport()
        }

        binding.buttonImport.setOnClickListener {
            startImport()
        }
    }

    private fun startExport() {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val currentDateStr = dateFormat.format(Date())
        val fileName = "trackify_export_$currentDateStr.json"

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }

        exportLauncher.launch(intent)
    }

    private fun exportData(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonExport.isEnabled = false
        binding.buttonImport.isEnabled = false

        lifecycleScope.launch {
            val success = dataManager.exportData(uri)

            binding.progressBar.visibility = View.GONE
            binding.buttonExport.isEnabled = true
            binding.buttonImport.isEnabled = true

            if (success) {
                val fileName = getFileName(uri)
                Toast.makeText(requireContext(), "Данные успешно экспортированы в $fileName", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Ошибка при экспорте данных", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }

        importLauncher.launch(intent)
    }

    private fun importData(uri: Uri) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Подтверждение импорта")
            .setMessage("Импорт данных заменит все существующие данные. Продолжить?")
            .setPositiveButton("Да") { _, _ ->
                proceedWithImport(uri)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun proceedWithImport(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonExport.isEnabled = false
        binding.buttonImport.isEnabled = false

        lifecycleScope.launch {
            val success = dataManager.importData(uri)

            binding.progressBar.visibility = View.GONE
            binding.buttonExport.isEnabled = true
            binding.buttonImport.isEnabled = true

            if (success) {
                Toast.makeText(requireContext(), "Данные успешно импортированы", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Ошибка при импорте данных", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        val cursor = requireContext().contentResolver.query(
            uri, null, null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    return it.getString(nameIndex)
                }
            }
        }

        return uri.lastPathSegment ?: "файл"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}