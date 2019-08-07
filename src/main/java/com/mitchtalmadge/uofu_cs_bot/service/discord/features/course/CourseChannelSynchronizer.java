package com.mitchtalmadge.uofu_cs_bot.service.discord.features.course;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSSuffix;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.Course;
import com.mitchtalmadge.uofu_cs_bot.service.discord.channel.ChannelSynchronizer;
import com.mitchtalmadge.uofu_cs_bot.util.CSNamingConventions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.ChannelManager;
import net.dv8tion.jda.core.managers.PermOverrideManager;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Implementation of {@link ChannelSynchronizer} for Courses.
 */
public class CourseChannelSynchronizer extends ChannelSynchronizer {

    private static final String COURSE_CATEGORY_NAME = "Courses";

    private CourseService courseService;

    @Autowired
    public CourseChannelSynchronizer(CourseService CourseService) {
        super("cs-", 0);
        this.courseService = CourseService;
    }

    /**
     * @return The category that all text-based Course Channels belong to.
     * May be null if the Category does not yet exist.
     */
    private Category getCourseTextCategory(Guild guild) {
        List<Category> categories = guild.getCategoriesByName(COURSE_CATEGORY_NAME, false);
        if (categories.size() > 0)
            return categories.get(0);

        return null;
    }

    @Override
    public Pair<Collection<Category>, Collection<String>> synchronizeChannelCategories(List<Category> categories) {
        // Create Collections for returning.
        Collection<Category> categoriesToRemove = new HashSet<>();
        Collection<String> categoriesToCreate = new HashSet<>();

        // Check if the Text Courses Category does not exist.
        if (categories.stream().noneMatch(category -> category.getName().equalsIgnoreCase(COURSE_CATEGORY_NAME))) {
            // Create the Category.
            categoriesToCreate.add(COURSE_CATEGORY_NAME);
        }

        // Return Collections.
        return Pair.of(categoriesToRemove, categoriesToCreate);
    }

    @Override
    public Pair<Collection<TextChannel>, Collection<String>> synchronizeTextChannels(List<TextChannel> filteredChannels) {

        // Create collections for returning.
        Collection<TextChannel> channelsToRemove = new HashSet<>();
        Collection<String> channelsToCreate = new HashSet<>();

        // Get all enabled Courses.
        Set<Course> enabledCourses = courseService.getEnabledCourses();

        // This set starts by containing all enabled Courses. Courses are removed one-by-one as their channels are found.
        // The remaining Courses which have not been removed must be created as new channels.
        Set<Course> missingCourses = new HashSet<>(enabledCourses);

        // Find the existing channels and delete invalid channels.
        filteredChannels.forEach(channel -> {
            try {
                // Parse the channel as a Course.
                Course course = new Course(channel.getName());

                // Remove the Course if it exists.
                if (missingCourses.contains(course)) {
                    missingCourses.remove(course);
                } else {
                    // Since the Course for this channel is not enabled, the channel should be removed.
                    channelsToRemove.add(channel);
                }
            } catch (Course.InvalidCourseNameException ignored) {
                // This channel is not a Course channel.
            }
        });

        // Convert the missing Courses into Channel names.
        missingCourses.forEach(course -> channelsToCreate.add(CSNamingConventions.toChannelName(course)));

        // Return collections.
        return Pair.of(channelsToRemove, channelsToCreate);
    }

    @Override
    public Collection<ChannelManager> updateChannelCategorySettings(List<Category> categories) {
        // TODO: Category Settings
        return null;
    }

    @Override
    public Collection<ChannelManager> updateTextChannelSettings(List<TextChannel> filteredChannels) {

        // Create Collection to be returned.
        Collection<ChannelManager> channelManagers = new HashSet<>();

        filteredChannels.forEach(textChannel -> {
            try {
                Course course = new Course(textChannel.getName());

                ChannelManager manager = textChannel.getManager()
                        .setName(CSNamingConventions.toChannelName(course))
                        .setParent(getCourseTextCategory(textChannel.getGuild()))
                        .setNSFW(false);

                channelManagers.add(manager);
            } catch (Course.InvalidCourseNameException ignored) {
                // This is not a course channel.
            }
        });

        // Return managers to be queued.
        return channelManagers;
    }

    @Override
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManager>> updateChannelCategoryPermissions(List<Category> categories) {
        // TODO: Category Permissions
        return null;
    }

    @Override
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManager>> updateTextChannelPermissions(List<TextChannel> filteredChannels) {

        // Create Collections for returning.
        Collection<PermissionOverride> permissionOverrides = new HashSet<>();
        Collection<PermissionOverrideAction> permissionOverrideActions = new HashSet<>();
        Collection<PermOverrideManager> permOverrideManagers = new HashSet<>();

        filteredChannels.forEach(textChannel -> {
            try {
                Course course = new Course(textChannel.getName());

                // Compute Permissions.
                Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManager>> updateResult = updateChannelPermissions(textChannel, course);

                // Append to Collections.
                permissionOverrides.addAll(updateResult.getLeft().getLeft());
                permissionOverrideActions.addAll(updateResult.getLeft().getRight());
                permOverrideManagers.addAll(updateResult.getRight());
            } catch (Course.InvalidCourseNameException ignored) {
                // This is not a course channel.
            }
        });

        // Return Collections.
        return Pair.of(Pair.of(permissionOverrides, permissionOverrideActions), permOverrideManagers);
    }

    /**
     * Ensures that permissions are set correctly for a specific class channel.
     *
     * @param channel      The channel.
     * @param channelClass The channel's associated Course instance.
     */
    private Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManager>> updateChannelPermissions(Channel channel, Course channelClass) {

        // Create collections for returning.
        Collection<PermissionOverride> permissionOverrides = new HashSet<>();
        Collection<PermissionOverrideAction> permissionOverrideActions = new HashSet<>();
        Collection<PermOverrideManager> permOverrideManagers = new HashSet<>();

        // Keeps track of whether the channel has a permission override for each suffix, and for @everyone (null key).
        Map<CSSuffix, Boolean> overrideDetectionMap = new HashMap<>();

        // Check each permission override.
        channel.getPermissionOverrides().forEach(override -> {
            // Delete all member overrides.
            if (override.isMemberOverride()) {
                permissionOverrides.add(override);
                return;
            }

            Role role = override.getRole();

            if (role.isPublicRole()) { // @everyone role
                overrideDetectionMap.put(null, true);

                PermOverrideManager manager = override.getManager();

                // Clear all permissions
                manager = manager.clear(Permission.ALL_PERMISSIONS);

                // Deny viewing
                manager = manager.deny(Permission.VIEW_CHANNEL);

                permOverrideManagers.add(manager);
            } else {
                try { // Class role
                    Course course = new Course(role.getName());

                    CSSuffix roleSuffix = CSSuffix.fromCourseName(role.getName());

                    overrideDetectionMap.put(roleSuffix, true);

                    PermOverrideManager manager = override.getManager();

                    // Clear all permissions
                    manager = manager.clear(Permission.ALL_PERMISSIONS);

                    // Allow viewing
                    manager = manager.grant(Permission.VIEW_CHANNEL);

                    permOverrideManagers.add(manager);
                } catch (Course.InvalidCourseNameException ignored) {
                    // This is not a course role. Delete its override.
                    permissionOverrides.add(override);
                }
            }
        });

        // Create the channel's @everyone role permission override if it does not exist.
        if (!overrideDetectionMap.getOrDefault(null, false)) {
            PermissionOverrideAction override = channel.createPermissionOverride(channel.getGuild().getPublicRole());
            override = override.setDeny(Permission.VIEW_CHANNEL);
            permissionOverrideActions.add(override);
        }

        // Create the channel's suffix role permission overrides if they do not exist.
        for (CSSuffix suffix : CSSuffix.values()) {
            if (!overrideDetectionMap.getOrDefault(suffix, false)) {
                PermissionOverrideAction override = channel.createPermissionOverride(
                        channel.getGuild().getRolesByName(CSNamingConventions.toRoleName(channelClass, suffix), true).get(0)
                );
                override = override.setAllow(Permission.VIEW_CHANNEL);
                permissionOverrideActions.add(override);
            }
        }

        // Return collections.
        return Pair.of(Pair.of(permissionOverrides, permissionOverrideActions), permOverrideManagers);
    }

    @Override
    public List<Category> updateChannelCategoryOrdering(List<Category> categories) {
        return null;
    }

    @Override
    public List<TextChannel> updateTextChannelOrdering(List<TextChannel> filteredChannels) {
        // Sort filtered channels by name.
        filteredChannels.sort(Comparator.comparing(Channel::getName));

        return filteredChannels;
    }

}
