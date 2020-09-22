package se325.assignment01.concert.service.mapper;


import se325.assignment01.concert.common.dto.UserDTO;
import se325.assignment01.concert.service.domain.User;

public class UserMapper {
    public static UserDTO convertToDTO(User user){


        // create dto using field info contained in a user object
        return new UserDTO(user.getUsername(), user.getPassword());
    }
}
