package com.meetup.meetupapi.controller;

import java.util.ArrayList;
import java.util.List;

import com.meetup.meetupapi.model.GroupMembership;
import com.meetup.meetupapi.model.MeetingPossibility;
import com.meetup.meetupapi.model.Meeting;
import com.meetup.meetupapi.model.MeetupGroup;
import com.meetup.meetupapi.model.UserAvailability;
import com.meetup.meetupapi.repo.GroupMembershipRepository;
import com.meetup.meetupapi.repo.MeetingRepository;
import com.meetup.meetupapi.repo.MeetupGroupRepository;
import com.meetup.meetupapi.repo.UserAvailabilityRepository;
import com.meetup.meetupapi.services.ScheduleEngine;
import com.meetup.meetupapi.services.fulldailyschedule;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups")
public class MeetupGroupController {

    private MeetupGroupRepository meetupGroupRepository;
    private GroupMembershipRepository groupMembershipRepository;
    private UserAvailabilityRepository userAvailabilityRepository;
    private MeetingRepository meetingRepository;
    public MeetupGroupController(
            MeetupGroupRepository meetupGroupRepository, 
            GroupMembershipRepository groupMembershipRepository,
            UserAvailabilityRepository userAvailabilityRepository,
            MeetingRepository meetingRepository){
        this.meetupGroupRepository = meetupGroupRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.userAvailabilityRepository = userAvailabilityRepository;
        this.meetingRepository = meetingRepository;
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/retrieve")
    public ResponseEntity<?> getMyData(@RequestParam("id") int userId){
        JSONObject response = new JSONObject();
        JSONArray groupArr = new JSONArray();

        int status = 200;
        List<MeetupGroup> userGroups;
        List<GroupMembership> members;
        List<Meeting> meetingList;

        try {
            userGroups = meetupGroupRepository.findMyGroups(userId);
            // for each group user is in, create member array of member obj
            for(MeetupGroup group : userGroups){

                // Each iteration, create a group object with an
                // array of members
                JSONObject groupObj = new JSONObject();
                JSONArray membersArr = new JSONArray();

                // find group ID, find members in that group
                Long groupId = group.getGroup_id();
                members = groupMembershipRepository.findMembers(groupId);

                // for each member in the group, put into member array
                for (GroupMembership groupMembership : members) {
                    JSONObject memberObj = new JSONObject();
                    memberObj.put("id", groupMembership.getUser().getId());
                    memberObj.put("name", groupMembership.getUser().getFullName());
                    memberObj.put("email", groupMembership.getUser().getEmail());
                    memberObj.put("phone", groupMembership.getUser().getPhone());
                    membersArr.add(memberObj);
                }

                // Now we have group info, members array, we must find
                // the meetings for each group
                JSONArray meetingArr = new JSONArray();
                meetingList = meetingRepository.findAllByGroupId(groupId);
                for(Meeting meeting: meetingList){
                    JSONObject meetingObj = new JSONObject();
                    meetingObj.put("id", meeting.getId());
                    meetingObj.put("creator", meeting.getUser().getFullName());
                    meetingObj.put("day", meeting.getMeeting_day());
                    meetingObj.put("start_time", meeting.getMeeting_start_time());
                    meetingObj.put("end_time", meeting.getMeeting_end_time());
                    meetingArr.add(meetingObj);
                }

                //Create an owner object for clarity
                JSONObject owner = new JSONObject();
                owner.put("id", group.getUser().getId());
                owner.put("username", group.getUser().getUsername());
                owner.put("fullname", group.getUser().getFullName());
                
                // Now we have found all the group members of this group
                // We place the group info, it's members, and it's meetings 
                // into the group object and start for the next group
                groupObj.put("id", group.getGroup_id());
                groupObj.put("name", group.getGroup_name());
                groupObj.put("owner", owner);
                groupObj.put("members", membersArr);
                groupObj.put("meetings", meetingArr);
                groupArr.add(groupObj);
            };

            response.put("groups", groupArr);
        } catch (Exception e){
            System.out.println("Query failed");
            status = 500;
            response.put("message", "Query Failed");
        }

        return ResponseEntity.status(status).body(response);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/add")
    public ResponseEntity<?> createAndJoin(
            @RequestParam("id") int userId,
            @RequestParam("name") String groupName){
        JSONObject response = new JSONObject();
        String stringData = "";
        int status = 200;

        // Create group
        try {
            int createQueryStatus = meetupGroupRepository.createGroup(groupName, userId);
            if(createQueryStatus > 0){
                stringData += "" + groupName + " was created successfully!";
                int joinQueryStatus = groupMembershipRepository.joinGroup(userId, groupName);
                if (joinQueryStatus > 0){
                    stringData += " You have now joined!";
                }
                response.put("data", stringData);
            } else {
                response.put("message", "Error creating group");
            }
        } catch (Exception e){
            status = 500;
            response.put("message", "Error creating group");
        }
        return ResponseEntity.status(status).body(response);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/join")
    public ResponseEntity<?> joinGroup(
            @RequestParam("id") int userId,
            @RequestParam("name") String groupName){
        JSONObject response = new JSONObject();
        int status = 200;
        try {
            int val = groupMembershipRepository.joinGroup(userId, groupName);
            if (val > 0){
                response.put("data", "You have joined " + groupName);
            }
            else {
                response.put("message", "This group does not exist. Was there a typo?");
            }
        } catch (Exception e){
            response.put("message", e.toString());
            status = 500;
        }
        System.out.println("\n" + response);
        return ResponseEntity.status(status).body(response);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/leave")
    public ResponseEntity<?> leaveGroup(
            @RequestParam("groupId") int groupId,
            @RequestParam("userId") int userId){
        
        JSONObject response = new JSONObject();
        int status = 200;
        try {
            System.out.println("Removing " + userId + "from group " + groupId);
            int val = groupMembershipRepository.deleteFromGroup(userId, groupId);
            if ( val > 0 ) {
                response.put("data", "You have been successfully removed from the group");
            }
            else {
                response.put("message", "Something went wrong");
                status = 500;
            }
        } catch (Exception e){
            status = 500;
            response.put("message", e.toString());
        }

        return ResponseEntity.status(status).body(response);
    }

    /*
        Here is where the user availability data is located. 
        Logic for calculating meetings should go here.

        Each availability belongs to a user which one can get
        by using getId() or any of its class methods. Parse
        this data into meeting possibilty code for calculations. 
        
        Print statements for right now to help visualize
    */
    @SuppressWarnings("unchecked")
    @PostMapping("/availabilities")
    public ResponseEntity<?> getAvailabilities(
            @RequestParam("id") Long groupId
    ){
        List<GroupMembership> members;
        List<UserAvailability> availabilitiesList;
        List<Long> userIds = new ArrayList<Long>();
        ArrayList<MeetingPossibility> allmeetingpossibilities = new ArrayList<MeetingPossibility>(); //will contain all meeting possibilities
        try {
            // For each membership, get userId
            members = groupMembershipRepository.findMembers(groupId);
            //for each day in week, creates a scheduleEngine instance
            ScheduleEngine SundayEngine = new ScheduleEngine("Sunday");
            ScheduleEngine MondayEngine = new ScheduleEngine("Monday");
            ScheduleEngine TuesdayEngine = new ScheduleEngine("Tuesday");
            ScheduleEngine WednesdayEngine = new ScheduleEngine("Wednesday");
            ScheduleEngine ThursdayEngine = new ScheduleEngine("Thursday");
            ScheduleEngine FridayEngine = new ScheduleEngine("Friday");
            ScheduleEngine SaturdayEngine = new ScheduleEngine("Saturday");
            for(GroupMembership membership: members){
                userIds.add(membership.getUser().getId());
            }
            // For each userId, get all availability
            for(Long id: userIds){
            	//Makes a full daily schedule for each day for every user, then adds the schedule to the appropriate engine
            	availabilitiesList = userAvailabilityRepository.findAllByUserId(id);
            	fulldailyschedule sundayschedule = new fulldailyschedule("Sunday", id, availabilitiesList);
            	SundayEngine.addSchedule(sundayschedule);
            	fulldailyschedule mondayschedule = new fulldailyschedule("Monday", id, availabilitiesList);
            	MondayEngine.addSchedule(mondayschedule);
            	fulldailyschedule tuesdayschedule = new fulldailyschedule("Tuesday", id, availabilitiesList);
            	TuesdayEngine.addSchedule(tuesdayschedule);
            	fulldailyschedule wednesdayschedule = new fulldailyschedule("Wednesday", id, availabilitiesList);
            	WednesdayEngine.addSchedule(wednesdayschedule);
            	fulldailyschedule thursdayschedule = new fulldailyschedule("Thursday", id, availabilitiesList);
            	ThursdayEngine.addSchedule(thursdayschedule);
            	fulldailyschedule fridayschedule = new fulldailyschedule("Friday", id, availabilitiesList);
            	FridayEngine.addSchedule(fridayschedule);
            	fulldailyschedule saturdayschedule = new fulldailyschedule("Saturday", id, availabilitiesList);
            	SaturdayEngine.addSchedule(saturdayschedule);
            	
            	
                for(UserAvailability availability: availabilitiesList){
                	
                    System.out.println(
                            "Availability: " +
                            availability.getUser().getFullName() + "\n" +
                            availability.getDay() + "\n" +
                            availability.getStart_time() + "\n" +
                            availability.getEnd_time() + "\n"
                    );      
                }
                
                
            }
            allmeetingpossibilities.addAll(SundayEngine.calculatemeeting(1)); //1 is a placeholder duration of 1 hour
        } catch (Exception e){
            System.out.println("Exception: " + e);
        }

        return ResponseEntity.status(200).body("ok");
    }
}