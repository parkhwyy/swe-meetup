package com.meetup.meetupapi.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;

@Entity 
@IdClass(GroupMembership.class)
@Table(name = "group_membership", uniqueConstraints = @UniqueConstraint(columnNames={}))
public class GroupMembership implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name="group_id", referencedColumnName = "group_id")
    private MeetupGroup group;
    
    @Id
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name="user_id", referencedColumnName = "id")
    private ApplicationUser user;

}

