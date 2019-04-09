package com.mitchtalmadge.uofu_cs_bot.service.discord.features.course;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSConstants;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSSuffix;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.Course;
import com.mitchtalmadge.uofu_cs_bot.service.discord.channel.ChannelSynchronizer;
import com.mitchtalmadge.uofu_cs_bot.util.CSNamingConventions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;
import net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Implementation of {@link ChannelSynchronizer} for Courses.
 */
public class CourseChannelSynchronizer extends ChannelSynchronizer {

    private static final String COURSE_TEXT_CATEGORY_NAME = "Courses (Text)";
    private static final String COURSE_VOICE_CATEGORY_NAME = "Courses (Voice)";

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
        List<Category> categories = guild.getCategoriesByName(COURSE_TEXT_CATEGORY_NAME, false);
        if (categories.size() > 0)
            return categories.get(0);

        return null;
    }

    /**
     * @return The category that all voice-based Course Channels belong to.
     * May be null if the Category does not yet exist.
     */
    private Category getCourseVoiceCategory(Guild guild) {
        List<Category> categories = guild.getCategoriesByName(COURSE_VOICE_CATEGORY_NAME, false);
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
        if (categories.stream().noneMatch(category -> category.getName().equalsIgnoreCase(COURSE_TEXT_CATEGORY_NAME))) {
            // Create the Category.
            categoriesToCreate.add(COURSE_TEXT_CATEGORY_NAME);
        }

        // Check if the Voice Courses Category does not exist.
        if (categories.stream().noneMatch(category -> category.getName().equalsIgnoreCase(COURSE_VOICE_CATEGORY_NAME))) {
            // Create the Category.
            categoriesToCreate.add(COURSE_VOICE_CATEGORY_NAME);
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
    public Pair<Collection<VoiceChannel>, Collection<String>> synchronizeVoiceChannels(List<VoiceChannel> filteredChannels) {

        // Create collections for returning.
        Collection<VoiceChannel> channelsToRemove = new HashSet<>();
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
    public Collection<ChannelManagerUpdatable> updateChannelCategorySettings(List<Category> categories) {
        // TODO: Category Settings
        return null;
    }

    @Override
    public Collection<ChannelManagerUpdatable> updateTextChannelSettings(List<TextChannel> filteredChannels) {

        // Create Collection to be returned.
        Collection<ChannelManagerUpdatable> channelManagerUpdatables = new HashSet<>();

        filteredChannels.forEach(textChannel -> {
            try {
                Course course = new Course(textChannel.getName());

                ChannelManagerUpdatable updater = textChannel.getManagerUpdatable();

                // Name
                updater = updater.getNameField().setValue(CSNamingConventions.toChannelName(course));

                // Category
                updater = updater.getParentField().setValue(getCourseTextCategory(textChannel.getGuild()));

                // NSFW Off
                updater = updater.getNSFWField().setValue(false);

                channelManagerUpdatables.add(updater);
            } catch (Course.InvalidCourseNameException ignored) {
                // This is not a course channel.
            }
        });

        // Return updatables to be queued.
        return channelManagerUpdatables;
    }

    @Override
    public Collection<ChannelManagerUpdatable> updateVoiceChannelSettings(List<VoiceChannel> filteredChannels) {

        // Create Collection to be returned.
        Collection<ChannelManagerUpdatable> channelManagerUpdatables = new HashSet<>();

        filteredChannels.forEach(voiceChannel -> {
            try {
                Course course = new Course(voiceChannel.getName());

                ChannelManagerUpdatable updater = voiceChannel.getManagerUpdatable();

                // Name
                updater = updater.getNameField().setValue(CSNamingConventions.toChannelName(course));

                // Category
                updater = updater.getParentField().setValue(getCourseVoiceCategory(voiceChannel.getGuild()));

                // Bitrate and User Limit
                updater = updater
                        .getBitrateField().setValue(CSConstants.CS_CHANNEL_VOICE_BITRATE)
                        .getUserLimitField().setValue(CSConstants.CS_CHANNEL_VOICE_USERLIMIT);

                channelManagerUpdatables.add(updater);
            } catch (Course.InvalidCourseNameException ignored) {
                // This is not a course channel.
            }
        });

        // Return updatables to be queued.
        return channelManagerUpdatables;
    }

    @Override
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateChannelCategoryPermissions(List<Category> categories) {
        // TODO: Category Permissions
        return null;
    }

    @Override
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateTextChannelPermissions(List<TextChannel> filteredChannels) {

        // Create Collections for returning.
        Collection<PermissionOverride> permissionOverrides = new HashSet<>();
        Collection<PermissionOverrideAction> permissionOverrideActions = new HashSet<>();
        Collection<PermOverrideManagerUpdatable> permOverrideManagerUpdatables = new HashSet<>();

        filteredChannels.forEach(textChannel -> {
            try {
                Course course = new Course(textChannel.getName());

                // Compute Permissions.
                Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateResult = updateChannelPermissions(textChannel, course);

                // Append to Collections.
                permissionOverrides.addAll(updateResult.getLeft().getLeft());
                permissionOverrideActions.addAll(updateResult.getLeft().getRight());
                permOverrideManagerUpdatables.addAll(updateResult.getRight());
            } catch (Course.InvalidCourseNameException ignored) {
                // This is not a course channel.
            }
        });

        // Return Collections.
        return Pair.of(Pair.of(permissionOverrides, permissionOverrideActions), permOverrideManagerUpdatables);
    }

    @Override
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateVoiceChannelPermissions(List<VoiceChannel> filteredChannels) {

        // Create Collections for returning.
        Collection<PermissionOverride> permissionOverrides = new HashSet<>();
        Collection<PermissionOverrideAction> permissionOverrideActions = new HashSet<>();
        Collection<PermOverrideManagerUpdatable> permOverrideManagerUpdatables = new HashSet<>();

        filteredChannels.forEach(voiceChannel -> {
            try {
                Course course = new Course(voiceChannel.getName());

                // Compute Permissions.
                Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateResult = updateChannelPermissions(voiceChannel, course);

                // Append to Collections.
                permissionOverrides.addAll(updateResult.getLeft().getLeft());
                permissionOverrideActions.addAll(updateResult.getLeft().getRight());
                permOverrideManagerUpdatables.addAll(updateResult.getRight());
            } catch (Course.InvalidCourseNameException ignored) {
                // This is not a course channel.
            }
        });

        // Return Collections.
        return Pair.of(Pair.of(permissionOverrides, permissionOverrideActions), permOverrideManagerUpdatables);
    }

    /**
     * Ensures that permissions are set correctly for a specific class channel.
     *
     * @param channel      The channel.
     * @param channelClass The channel's associated Course instance.
     */
    private Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateChannelPermissions(Channel channel, Course channelClass) {

        // Create collections for returning.
        Collection<PermissionOverride> permissionOverrides = new HashSet<>();
        Collection<PermissionOverrideAction> permissionOverrideActions = new HashSet<>();
        Collection<PermOverrideManagerUpdatable> permOverrideManagerUpdatables = new HashSet<>();

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

                PermOverrideManagerUpdatable manager = override.getManagerUpdatable();

                // Clear all permissions
                manager = manager.clear(Permission.ALL_PERMISSIONS);

                // Deny viewing
                manager = manager.deny(Permission.VIEW_CHANNEL);

                permOverrideManagerUpdatables.add(manager);
            } else {
                try { // Class role
                    Course course = new Course(role.getName());

                    CSSuffix roleSuffix = CSSuffix.fromCourseName(role.getName());

                    overrideDetectionMap.put(roleSuffix, true);

                    PermOverrideManagerUpdatable manager = override.getManagerUpdatable();

                    // Clear all permissions
                    manager = manager.clear(Permission.ALL_PERMISSIONS);

                    // Allow viewing
                    manager = manager.grant(Permission.VIEW_CHANNEL);

                    permOverrideManagerUpdatables.add(manager);
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
        return Pair.of(Pair.of(permissionOverrides, permissionOverrideActions), permOverrideManagerUpdatables);
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

    @Override
    public List<VoiceChannel> updateVoiceChannelOrdering(List<VoiceChannel> filteredChannels) {
        // Sort filtered channels by name.
        filteredChannels.sort(Comparator.comparing(Channel::getName));

        return filteredChannels;
    }

}
