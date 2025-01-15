# Focus Buddy

Focus Buddy is an Android application designed to help users manage their projects and tasks efficiently. The app allows users to create projects, add tasks, and track their progress.

## Features

- **Project Management**: Create, edit, and delete projects.
- **Task Management**: Add, edit, and delete tasks within projects.
- **Progress Tracking**: Track the completion percentage of tasks and projects.
- **Daily Tasks**: View tasks scheduled for today.
- **Notifications**: Receive notifications for upcoming tasks and deadlines.

## Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/Sheild007/FocusBuddy-a-task-managemnt-app
    ```
2. Open the project in Android Studio.
3. Build and run the project on an Android device or emulator.

## Usage

- **Creating a Project**: Click on the "New Project" button, enter the project details, and save.
- **Adding Tasks**: Open a project, click on the "New Task" button, enter the task details, and save.
- **Editing Tasks**: Click on a task to view its details and make changes.
- **Tracking Progress**: View the progress of tasks and projects on the main screen.

## Project Structure

- `MainActivity.java`: The main activity that hosts the navigation drawer and view pager.
- `ProjectAdapter.java`: Adapter for displaying projects in a RecyclerView.
- `TaskAdapter.java`: Adapter for displaying tasks in a RecyclerView.
- `projectDetails.java`: Activity for displaying and editing project details.
- `taskDetails.java`: Activity for displaying and editing task details.
- `TodayFragment.java`: Fragment for displaying tasks scheduled for today.
- `AllTasksFragment.java`: Fragment for displaying all tasks.
- `ViewPagerAdapter.java`: Adapter for managing fragments in the view pager.

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature-branch`).
3. Make your changes and commit them (`git commit -m 'Add new feature'`).
4. Push to the branch (`git push origin feature-branch`).
5. Open a pull request.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.

## Contact

For any inquiries or feedback, please contact [Sheild007](https://github.com/Shield007).
