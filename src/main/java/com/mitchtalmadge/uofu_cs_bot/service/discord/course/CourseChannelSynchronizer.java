package com.mitchtalmadge.uofu_cs_bot.service.discord.course;

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
import java.util.stream.Collectors;

/**
 * Implementation of {@link ChannelSynchronizer} for Courses.
 */
public class CourseChannelSynchronizer extends ChannelSynchronizer {

    private static final String COURSE_CATEGORY_NAME = "Courses";

    private CourseService courseService;

    @Autowired
    public CourseChannelSynchronizer(CourseService CourseService) {
        this.courseService = CourseService;
    }

    /**
     * @return The category that all Course Channels belong to. May be null if the Category does not yet exist.
     */
    private Category getCoursesCategory(Guild guild) {
        List<Category> categories = guild.getCategoriesByName(COURSE_CATEGORY_NAME, false);
        if (categories.size() > 0)
            return categories.get(0);

        return null;
    }

    @Override
    public Pair<Collection<Category>, Collection<String>> synchronizeChannelCategories(List<Category> categories) {
        // TODO: Create Courses Voice Category

        // Create Collections for returning.
        Collection<Category> categoriesToRemove = new HashSet<>();
        Collection<String> categoriesToCreate = new HashSet<>();

        // Check if the Courses Category does not exist.
        if (categories.stream().noneMatch(category -> category.getName().equalsIgnoreCase(COURSE_CATEGORY_NAME))) {
            // Create the Courses Category.
            categoriesToCreate.add(COURSE_CATEGORY_NAME);
        }

        // Return Collections.
        return Pair.of(categoriesToRemove, categoriesToCreate);
    }

    @Override
    public Pair<Collection<TextChannel>, Collection<String>> synchronizeTextChannels(List<TextChannel> textChannels) {

        // Create collections for returning.
        Collection<TextChannel> channelsToRemove = new HashSet<>();
        Collection<String> channelsToCreate = new HashSet<>();

        // Get all enabled Courses.
        Set<Course> enabledCourses = courseService.getEnabledCourses();

        // This set starts by containing all enabled Courses. Courses are removed one-by-one as their channels are found.
        // The remaining Courses which have not been removed must be created as new channels.
        Set<Course> missingCourses = new HashSet<>(enabledCourses);

        // Find the existing channels and delete invalid channels.
        textChannels.forEach(channel -> {
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
    public Pair<Collection<VoiceChannel>, Collection<String>> synchronizeVoiceChannels(List<VoiceChannel> voiceChannels) {

        // Create collections for returning.
        Collection<VoiceChannel> channelsToRemove = new HashSet<>();
        Collection<String> channelsToCreate = new HashSet<>();

        // Get all enabled Courses.
        Set<Course> enabledCourses = courseService.getEnabledCourses();

        // This set starts by containing all enabled Courses. Courses are removed one-by-one as their channels are found.
        // The remaining Courses which have not been removed must be created as new channels.
        Set<Course> missingCourses = new HashSet<>(enabledCourses);

        // Find the existing channels and delete invalid channels.
        voiceChannels.forEach(channel -> {
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
    public Collection<ChannelManagerUpdatable> updateTextChannelSettings(List<TextChannel> textChannels) {

        // Create Collection to be returned.
        Collection<ChannelManagerUpdatable> channelManagerUpdatables = new HashSet<>();

        textChannels.forEach(textChannel -> {
            try {
                Course course = new Course(textChannel.getName());

                ChannelManagerUpdatable updater = textChannel.getManagerUpdatable();

                // Name
                updater = updater.getNameField().setValue(CSNamingConventions.toChannelName(course));

                // Category
                updater = updater.getParentField().setValue(getCoursesCategory(textChannel.getGuild()));

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
    public Collection<ChannelManagerUpdatable> updateVoiceChannelSettings(List<VoiceChannel> voiceChannels) {

        // Create Collection to be returned.
        Collection<ChannelManagerUpdatable> channelManagerUpdatables = new HashSet<>();

        voiceChannels.forEach(voiceChannel -> {
            try {
                Course course = new Course(voiceChannel.getName());

                ChannelManagerUpdatable updater = voiceChannel.getManagerUpdatable();

                // Name
                updater = updater.getNameField().setValue(CSNamingConventions.toChannelName(course));

                // Category
                updater = updater.getParentField().setValue(getCoursesCategory(voiceChannel.getGuild()));

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
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateTextChannelPermissions(List<TextChannel> textChannels) {

        // Create Collections for returning.
        Collection<PermissionOverride> permissionOverrides = new HashSet<>();
        Collection<PermissionOverrideAction> permissionOverrideActions = new HashSet<>();
        Collection<PermOverrideManagerUpdatable> permOverrideManagerUpdatables = new HashSet<>();

        textChannels.forEach(textChannel -> {
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
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateVoiceChannelPermissions(List<VoiceChannel> voiceChannels) {

        // Create Collections for returning.
        Collection<PermissionOverride> permissionOverrides = new HashSet<>();
        Collection<PermissionOverrideAction> permissionOverrideActions = new HashSet<>();
        Collection<PermOverrideManagerUpdatable> permOverrideManagerUpdatables = new HashSet<>();

        voiceChannels.forEach(voiceChannel -> {
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

                    CSSuffix roleSuffix = CSSuffix.fromClassName(role.getName());

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
    public List<TextChannel> updateTextChannelOrdering(List<TextChannel> textChannels) {

        // Partition the channels into two, based on whether or not they are class channels.
        Map<Boolean, List<TextChannel>> partitionedChannels = textChannels.stream().collect(Collectors.partitioningBy(textChannel -> {
            // Attempt to parse the channel as a Course. If successful, return true.
            try {
                new Course(textChannel.getName());
                return true;
            } catch (Course.InvalidCourseNameException ignored) {
                return false;
            }
        }));

        // Combine the Channels back together with the Course Channels in order at the bottom.
        // Do not re-order the other Channels; we do not care about their order.

        // Add non class channels.
        List<TextChannel> orderedTextChannels = new ArrayList<>(partitionedChannels.get(false));

        // Sort class channels before adding
        List<TextChannel> courseChannels = partitionedChannels.get(true);
        courseChannels.sort(Comparator.comparing(Channel::getName));

        // Add class channels
        orderedTextChannels.addAll(courseChannels);

        return orderedTextChannels;
    }

    @Override
    public List<VoiceChannel> updateVoiceChannelOrdering(List<VoiceChannel> voiceChannels) {

        // Partition the channels into two, based on whether or not they are class channels.
        Map<Boolean, List<VoiceChannel>> partitionedChannels = voiceChannels.stream().collect(Collectors.partitioningBy(voiceChannel -> {
            // Attempt to parse the channel as a Course. If successful, return true.
            try {
                new Course(voiceChannel.getName());
                return true;
            } catch (Course.InvalidCourseNameException ignored) {
                return false;
            }
        }));

        // Combine the Channels back together with the Course Channels in order at the bottom.
        // Do not re-order the other Channels; we do not care about their order.

        // Add non class channels.
        List<VoiceChannel> orderedVoiceChannels = new ArrayList<>(partitionedChannels.get(false));

        // Sort class channels before adding
        List<VoiceChannel> courseChannels = partitionedChannels.get(true);
        courseChannels.sort(Comparator.comparing(Channel::getName));

        // Add class channels
        orderedVoiceChannels.addAll(courseChannels);

        return orderedVoiceChannels;
    }

}