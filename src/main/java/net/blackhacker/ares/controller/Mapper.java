package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.*;
import net.blackhacker.ares.model.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class Mapper {

    public FeedDTO toFeedDTO(Feed feed) {
        if (feed == null) return null;
        FeedDTO dto = new FeedDTO();
        dto.setTitle(feed.getTitle());
        dto.setDescription(feed.getDescription());
        dto.setLink(feed.getLink());
        dto.setImage(feed.getImage());
        dto.setPodcast(feed.isPodcast());
        dto.setLastModified(feed.getLastModified());
        if (feed.getItems() != null) {
            dto.setItems(feed.getItems().stream()
                    .map(this::toFeedItemDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public Feed toFeed(FeedDTO dto) {
        if (dto == null) return null;
        Feed feed = new Feed();
        feed.setTitle(dto.getTitle());
        feed.setDescription(dto.getDescription());
        feed.setLink(dto.getLink());
        feed.setImage(dto.getImage());
        feed.setPodcast(dto.isPodcast());
        feed.setLastModified(dto.getLastModified());

        if (dto.getItems() != null) {
            feed.setItems(dto.getItems().stream()
                    .map(this::toFeedItem)
                    .collect(Collectors.toList()));
        }

        return feed;
    }

    public FeedItemDTO toFeedItemDTO(FeedItem item) {
        if (item == null) return null;

        FeedItemDTO dto = new FeedItemDTO();
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setLink(item.getLink());
        dto.setImage(item.getImage());
        dto.setDate(item.getDate());
        return dto;
    }

    public FeedItem toFeedItem(FeedItemDTO dto) {
        if (dto == null) return null;

        FeedItem item = new FeedItem();
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setLink(dto.getLink());
        item.setImage(dto.getImage());
        item.setDate(dto.getDate());
        return item;
    }

    public UserDTO toUserDTO(User user) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setEmail(user.getEmail());
        // Password should generally not be mapped back to DTO for security
        if (user.getFeeds() != null) {
            dto.setFeeds(user.getFeeds().stream()
                    .map(this::toFeedDTO)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    public User toUser(UserDTO dto) {
        if (dto == null) return null;

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        // Feeds and Roles usually require looking up entities from repositories
        return user;
    }

    public RoleDTO toRoleDTO(Role role) {
        if (role == null) return null;

        RoleDTO dto = new RoleDTO();
        dto.setName(role.getName());
        dto.setChildren(role.getSubRoles().stream()
                .map(this::toRoleDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    public Role toRole(RoleDTO dto,Role parent) {
        if (dto == null) return null;

        Role role = new Role();
        role.setName(dto.getName());
        role.setParentRole(parent);
        role.setSubRoles(dto.getChildren().stream()
                .map(child -> toRole(child,role))
                .collect(Collectors.toList()));
        return role;
    }

    public Role toRole(RoleDTO dto){
        if (dto == null) return null;

        return toRole(dto,null);
    }

    public AdminDTO toAdminDTO(Admins admin) {
        if (admin == null) return null;

        AdminDTO dto = new AdminDTO();
        dto.setName(admin.getName());
        dto.setEmail(admin.getEmail());
        // Password should generally not be mapped back to DTO for security
        if (admin.getRoles() != null) {
            dto.setRoles(admin.getRoles().stream()
                    .map(this::toRoleDTO)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    public Admins toAdmins(AdminDTO dto) {
        if (dto == null) return null;

        Admins admin = new Admins();
        admin.setName(dto.getName());
        admin.setEmail(dto.getEmail());
        admin.setPassword(dto.getPassword());
        // Roles usually require looking up entities from repositories
        return admin;
    }
}
