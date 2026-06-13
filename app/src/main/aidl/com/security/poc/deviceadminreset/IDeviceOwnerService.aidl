package com.security.poc.deviceadminreset;

interface IDeviceOwnerService {
    boolean resetPassword(String newPassword);
    boolean isActive();
}
