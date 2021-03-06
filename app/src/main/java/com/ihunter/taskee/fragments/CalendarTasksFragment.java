package com.ihunter.taskee.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.ihunter.taskee.Constants;
import com.ihunter.taskee.R;
import com.ihunter.taskee.activities.MainActivity;
import com.ihunter.taskee.adapters.TaskItemAdapter;
import com.ihunter.taskee.data.Task;
import com.ihunter.taskee.interfaces.views.CalendarTasksFragmentView;
import com.ihunter.taskee.services.RealmService;
import com.ihunter.taskee.ui.EmptyRecyclerView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Master Bison on 1/5/2017.
 */

public class CalendarTasksFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener, CompactCalendarView.CompactCalendarViewListener, CalendarTasksFragmentView, DatePickerDialog.OnDateSetListener {


    private RealmService realmService;
    private TaskItemAdapter calendarPlansAdapter;
    private boolean isExpanded = true;
    private Calendar lastDateSelected;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_title)
    AppCompatTextView toolbarTitle;

    @BindView(R.id.toolbar_calendar_indicator)
    View toolbarChevron;

    @BindView(R.id.toolbar_calendar)
    CompactCalendarView calendarView;

    @BindView(R.id.todo_list_empty_view)
    LinearLayout todoListEmptyView;

    @BindView(R.id.plansList)
    EmptyRecyclerView plansList;

    @OnClick(R.id.toolbar_expand_container)
    protected void expandToolbarClick() {
        appBar.setExpanded(!isExpanded);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        realmService = new RealmService(getActivity());
        lastDateSelected = Calendar.getInstance();
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        ButterKnife.bind(this, view);
        setupToolbar();
        setupAppBar();
        setupPlansList();
        calendarView.setListener(this);
        refreshEvents();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpView();
        refreshEvents();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.calendar_view_menu, menu);
//        menu.findItem(R.id.select_date).getIcon().setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white), PorterDuff.Mode.SRC_IN);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.select_date:
                DatePickerDialog.newInstance(CalendarTasksFragment.this,
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                        .show(getActivity().getFragmentManager(), "Datepickerdialog");
                ;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpView() {
        calendarPlansAdapter.replacePlansList(realmService.getResultsOnDay(lastDateSelected.getTimeInMillis()));
    }

    private void setToolbarTitle(String string) {
        toolbarTitle.setText(string);
    }

    private void setupToolbar(){
        ((MainActivity) getActivity()).setToolbar(toolbar);
        setToolbarTitle(Constants.getShortDate(lastDateSelected.getTimeInMillis()));
    }

    private void setupAppBar(){
        appBar.setExpanded(true);
        appBar.addOnOffsetChangedListener(this);
    }

    private void setupPlansList(){
        todoListEmptyView.findViewById(R.id.empty_image).setVisibility(View.GONE);
        calendarPlansAdapter = new TaskItemAdapter(getActivity());
        calendarPlansAdapter.setCalendarTaskFragmentView(this);
        plansList.setLayoutManager(new LinearLayoutManager(getContext()));
        plansList.setAdapter(calendarPlansAdapter);
        plansList.setEmptyView(todoListEmptyView);
    }

    private void animateChevron() {
        toolbarChevron.animate().rotation(isExpanded ? 0 : 180).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    private Calendar toCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    private void refreshEvents() {
        List<Event> events = new ArrayList<>();
        events.clear();
        for (Task plan : realmService.getAllTasks()) {
            events.add(new Event(Color.parseColor("#" + plan.getColor()), plan.getTimestamp()));
        }
        calendarView.removeAllEvents();
        calendarView.addEvents(events);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0) {
            isExpanded = true;
        } else {
            isExpanded = false;
        }
        animateChevron();
    }

    @Override
    public void onDayClick(Date dateClicked) {
        lastDateSelected = toCalendar(dateClicked);
        setUpView();
    }

    @Override
    public void onMonthScroll(Date firstDayOfNewMonth) {
        lastDateSelected = toCalendar(firstDayOfNewMonth);
        setUpView();
    }

    @Override
    public void onRefreshEvents() {
        refreshEvents();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendarView.setCurrentDate(calendar.getTime());
        lastDateSelected = calendar;
        setUpView();
    }

}
