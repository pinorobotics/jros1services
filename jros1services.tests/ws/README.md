Based on [ROS Tutorials](https://github.com/ros/ros_tutorials.git) version 0.10.2

# Build

```bash
sudo apt install ros-noetic-catkin
catkin_make
source devel/setup.zsh
```

# Run

Client:
```bash
rosrun add_two_ints_client add_two_ints_client_node
```

Server:
```bash
rosrun add_two_ints_server add_two_ints_server_node
```
