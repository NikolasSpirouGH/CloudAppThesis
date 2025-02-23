package com.cloud_ml_app_thesis.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import javax.annotation.processing.Generated;
import java.util.List;

@Entity
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;



    /*

      URL: https://stackoverflow.com/questions/1656113/hibernate-recursive-many-to-many-association-with-the-same-entity

       @ManyToMany to self is rather confusing because the way you'd normally model this differs from the "Hibernate" way. Your problem is you're missing another collection.

        Think of it this way - if you're mapping "author" / "book" as many-to-many, you need "authors" collection on Book and "books" collection on Author. In this case, your "User" entity
         represents both ends of a relationship; so you need "my friends" and "friend of" collections:


        @ManyToMany
        @JoinTable(name="tbl_friends",
         joinColumns=@JoinColumn(name="personId"),
         inverseJoinColumns=@JoinColumn(name="friendId")
        )
        private List<User> friends;

        @ManyToMany
        @JoinTable(name="tbl_friends",
         joinColumns=@JoinColumn(name="friendId"),
         inverseJoinColumns=@JoinColumn(name="personId")
        )
        private List<User> friendOf;
        You can still use the same association table, but note that join / inverseJon columns are swapped on collections.

        The "friends" and "friendOf" collections may or may not match (depending on whether your "friendship" is always mutual) and you don't have to expose them this way in your API, of course, but that's the way to map it in Hibernate.

        Share
        Improve this answer
        Follow
   */
    private List<Category> parentCategory;
}
