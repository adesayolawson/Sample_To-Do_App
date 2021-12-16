package com.adelawson.youverifynotes.ui.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.adelawson.youverifynotes.NotificationHelper
import com.adelawson.youverifynotes.R
import com.adelawson.youverifynotes.data.localSource.Task
import com.adelawson.youverifynotes.data.localSource.TaskViewModel
import com.adelawson.youverifynotes.databinding.FragmentNewTodoBinding
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class NewTodoFragment:Fragment(), DatePickerDialog.OnDateSetListener{
    private lateinit var binding: FragmentNewTodoBinding
    private lateinit var date_txv :TextView
    private lateinit var taskPriority:String
    private lateinit var taskCategory: String
    private val viewModel by viewModels<TaskViewModel>()
    private var taskDate:String? = null
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private var timeStr:String? = null
    private var mYear:Int= 0
    private var mMonth:Int= 0
    private var mDay:Int = 0
    private var mFirstHour:Int = 0
    private var mFirstMinute:Int = 0
    private var mSecondHour:Int= 0
    private var mSecondMinute: Int = 0

    private val notificationHelper = NotificationHelper()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewTodoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //nav setup
        navHostFragment =  activity?.supportFragmentManager?.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        hideNav()
        val taskNameEditText = binding.tasknameEdtx
        val taskDescriptionEditText = binding.taskdescriptionEdtx
        val taskReminderSwitch = binding.remindMeSwitch
        val remindMeContainer = binding.remindMe
        date_txv = binding.dateTxv




        val priorityToggle = binding.priorityToggle
        priorityToggle.selectButton(R.id.low_priority)
        taskPriority = getString(R.string.low_priority)
        priorityToggle.setOnSelectListener {
            val selectedToggle = priorityToggle.selectedButtons[0]
            when(selectedToggle.id){
                binding.lowPriority.id-> taskPriority = getString(R.string.low_priority)
                binding.midPriority.id-> taskPriority= getString(R.string.mid_priority)
                binding.highPriority.id-> taskPriority= getString(R.string.high_priority)
            }

        }

        val categoryToggle = binding.categoryToggle
        taskCategory = getString(R.string.work_cat)
        categoryToggle.selectButton(R.id.work_toggle)
        categoryToggle.setOnSelectListener {
            val selectedToggle = categoryToggle.selectedButtons[0]
            when(selectedToggle.id){
                binding.workToggle.id-> taskCategory = getString(R.string.work_cat)
                binding.schoolToggle.id-> taskCategory= getString(R.string.school_cat)
                binding.familyToggle.id-> taskCategory= getString(R.string.family_cat)
            }
        }


        val datePicker = binding.datePicker
        datePicker.setOnClickListener {
            selectDate()
        }

        val alarmPicker = binding.alarmPicker
        alarmPicker.setOnClickListener {
            selectAlarm1()
        }

        remindMeContainer.setOnClickListener {
            taskReminderSwitch.toggle()
        }

        val addTaskFab = binding.addtaskFab
        addTaskFab.setOnClickListener {
            val taskDescription = taskDescriptionEditText.text.toString()
            val taskName = taskNameEditText.text.toString()
            if (!(TextUtils.isEmpty(taskDescription) && TextUtils.isEmpty(taskName))
                && !(taskDate.isNullOrBlank() && mFirstHour==0) )
                {Toast.makeText(requireContext(), "task created successfully ", Toast.LENGTH_SHORT).show()
                createTask(
                    taskPriority = taskPriority, taskReminder = taskReminderSwitch.isChecked,
                    taskName = taskName, taskDescription = taskDescription,
                    taskCategory = taskCategory, taskDate = taskDate!!,
                    taskAlarmTime = "${parseTime(mFirstHour)}:${parseTime(mFirstMinute)}", taskDuration = calculateTaskDuration(),
                    taskTimeRange = timeStr!!
                )
                navigateToHome()

            }else if (TextUtils.isEmpty(taskName)){
                Toast.makeText(requireContext(), "Please enter a task name", Toast.LENGTH_SHORT).show()
            }else if (TextUtils.isEmpty(taskDescription)){
                Toast.makeText(requireContext(), "Please enter a Description", Toast.LENGTH_SHORT).show()
            }else if (taskDate.isNullOrBlank()){
                Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show()
            }else if (mFirstHour==0){
                Toast.makeText(requireContext(), "Please select a time", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun selectDate(){
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val datePickerDialog = DatePickerDialog(requireContext(), this, year, month,day)
        datePickerDialog.show()

    }

    private fun selectAlarm1(){
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val timeDialog= TimePickerDialog(requireContext(), timeSetListener1, hour, minute, true)
        timeDialog.show()

    }

    private fun selectAlarm2(){
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val timeDialog= TimePickerDialog(requireContext(), timeSetListener2, hour, minute, true)
        timeDialog.show()
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mYear= year
        mMonth = month
        mDay = dayOfMonth
        val newCal = Calendar.getInstance()
        newCal.set(Calendar.YEAR,year)
        newCal.set(Calendar.MONTH,month)
        newCal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
        val sdf = SimpleDateFormat("EEEE d, LLLL", Locale.getDefault())
        val date = sdf.format(newCal.time).toString()
        date_txv.text = date
        taskDate = date

    }
    private val timeSetListener1 = TimePickerDialog.OnTimeSetListener { timePicker, hour, min ->
        mFirstHour = hour
        mFirstMinute = min
        selectAlarm2()
    }
    private val timeSetListener2 = TimePickerDialog.OnTimeSetListener { timePicker, hour, min ->
        mSecondHour = hour
        mSecondMinute = min
        val firstHourstr = parseTime(mFirstHour)
        val secondHourstr= parseTime(mSecondHour)
        val firstMinStr = parseTime(mFirstMinute)
        val secondMinStr = parseTime(mSecondMinute)
        timeStr = "$firstHourstr:$firstMinStr - $secondHourstr:$secondMinStr"

        binding.timeTxv.text = timeStr

    }

    private fun addNotification(task:Task){

        val dateSelected = Calendar.getInstance()
        dateSelected.set(mYear,mMonth,mDay,mFirstHour,mFirstMinute)
        val curTime = Calendar.getInstance()
        val diff = dateSelected.timeInMillis.minus(curTime.timeInMillis)

        notificationHelper.createNotificationChannel(requireContext())
        notificationHelper.createTaskNotification(task,diff,requireContext())



    }

    private fun parseTime(int: Int):String{
        val time = (if (int>=10) "$int" else "0$int")
        return time
    }

    private fun hideNav(){
        val bottomNav = activity?.findViewById<BottomAppBar>(R.id.bottomAppBar)
        bottomNav?.visibility = View.GONE
        val bottomFab = activity?.findViewById<FloatingActionButton>(R.id.new_note_fab)
        bottomFab?.visibility = View.GONE

    }

    private fun createTask( taskName:String,
                       taskDescription:String,
                       taskCategory:String,
                       taskPriority:String,
                       taskReminder:Boolean,
                       taskDate: String,taskAlarmTime:String,taskDuration:String,taskTimeRange:String
    ){
        val newTask = Task (taskName = taskName, taskDescription = taskDescription,
            taskCategory = taskCategory, taskDate = taskDate, taskReminder = taskReminder,
            taskPriority = taskPriority, taskID = 0, isTaskDone = false, taskAlarmTime = taskAlarmTime,
        taskDuration = taskDuration, taskTimeRange = taskTimeRange)

        if (newTask.taskReminder){
            addNotification(newTask)
        }
        viewModel.addTask(newTask)
    }
    private fun navigateToHome(){
        val navAction = NewTodoFragmentDirections.actionNewTodoFragmentToHomeScreenFragment()
        navController.navigate(navAction)
    }

    private fun calculateTaskDuration():String{
        val t1  = ((mFirstHour*60)+mFirstMinute)
        val t2 = ((mSecondHour*60)+ mSecondMinute)
        val t3 = (if(t1>t2) t1-t2 else t2-t1)
        val hr = t3/60
        val mn = t3%60

        val hour = (if(hr>1)"$hr hours" else "$hr hour")
        val min = (if(mn>1)"$mn mins" else "$mn min" )

        val duration = (if (mn>0)"$hour, $min" else hour)
        return duration
    }

}