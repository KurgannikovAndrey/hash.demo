package com.me.hash.demo.services

interface UserInputProcessingService {
    public void boot(String bootArgument)
    public void publish(String findArgument, String storePath, String name)
    public void download(String findArgument, String storePath, String fileName)
    public String debug(String query)
}