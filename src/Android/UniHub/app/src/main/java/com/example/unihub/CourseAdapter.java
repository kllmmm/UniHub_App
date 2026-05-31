package com.example.unihub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private List<Course> courses;
    private SemesterAdapter.OnCourseClickListener listener;

    public CourseAdapter(List<Course> courses, SemesterAdapter.OnCourseClickListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtGrade;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtCourseName);
            txtGrade = itemView.findViewById(R.id.txtGrade);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Course course = courses.get(position);

        holder.txtName.setText(course.getName());
        holder.txtGrade.setText(String.valueOf(course.getGrade() * 10));

        if (!course.getPassed() && course.getDeclared() ) {
            holder.txtGrade.setTextColor(android.graphics.Color.parseColor("#FF0000"));
        }
        else if (!course.getDeclared()) {
            holder.txtGrade.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
        }



        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCourseClick(course);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courses != null ? courses.size() : 0;
    }
}