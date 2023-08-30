package com.example.springbatchremotechunk.worker;

import lombok.Data;

import java.io.Serializable;

@Data
public class Customer implements Serializable
{
    private int id;

    public Customer(int id)
    {
        this.id = id;
    }
}
