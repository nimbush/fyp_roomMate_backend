package com.apu.roommate_filter.config;

import com.apu.roommate_filter.model.ServiceContent;
import com.apu.roommate_filter.model.ServiceType;
import com.apu.roommate_filter.repository.ServiceContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Seeds the static content screens. Idempotent: only inserts a {@link ServiceType} that has no row
 * yet, so edits made in the database are never overwritten on restart.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceContentSeeder implements ApplicationRunner {

    private final ServiceContentRepository serviceContentRepository;

    @Override
    public void run(ApplicationArguments args) {
        defaults().forEach((type, titleAndBody) -> {
            if (serviceContentRepository.findByService(type).isPresent()) return;

            ServiceContent content = new ServiceContent();
            content.setService(type);
            content.setTitle(titleAndBody[0]);
            content.setContent(titleAndBody[1]);
            serviceContentRepository.save(content);
            log.info("Seeded default content for service: {}", type);
        });
    }

    private Map<ServiceType, String[]> defaults() {
        return Map.of(
                ServiceType.ABOUT, new String[]{
                        "About RoomMate Finder",
                        """
                        RoomMate Finder helps students and young professionals find a compatible \
                        person to share a home with.

                        Instead of scrolling through endless listings, you build a short lifestyle \
                        profile — your budget, sleeping schedule, cleanliness, diet, hobbies and \
                        preferences. We compare that profile against everyone else's using a \
                        weighted compatibility algorithm and show you the best fits first.

                        You swipe through suggested roommates. When two people like each other, \
                        it becomes a match and you can chat in the app.

                        This application was built as a final-year project at Asia Pacific \
                        University."""
                },
                ServiceType.PRIVACY, new String[]{
                        "Privacy Policy",
                        """
                        We collect only what the app needs to match you with a roommate: your name, \
                        email address, phone number, the lifestyle details you enter on your \
                        profile, and the photos you upload.

                        How we use it. Your profile details are used to calculate compatibility \
                        scores and to show your card to other users in their discovery deck. Your \
                        email address is used to verify your account and to send password resets.

                        What we never do. We do not sell your data, and we do not share it with \
                        advertisers.

                        Who can see what. Other users can see your profile details, photos and \
                        first and last name. They cannot see your email address or phone number. \
                        Messages are visible only to the two people in a match.

                        Security. Passwords are stored using BCrypt hashing and are never readable \
                        by us. Access to the app is authenticated with a signed token.

                        Your control. You can edit your profile at any time. To have your account \
                        and data deleted, contact us through the Help and Support screen."""
                },
                ServiceType.TERMS, new String[]{
                        "Terms and Conditions",
                        """
                        By creating an account you agree to the following.

                        Eligibility. You must be at least 18 years old and provide accurate \
                        information about yourself.

                        Your conduct. You agree not to harass other users, impersonate anyone, post \
                        offensive or misleading content, or use the app for anything other than \
                        finding a roommate.

                        Your content. You keep ownership of the photos and text you upload. By \
                        uploading them you allow us to display them to other users of the app.

                        Matching. Compatibility scores are a guide based on the information you \
                        provide. They are not a guarantee of suitability. You are responsible for \
                        meeting safely and for any arrangement you enter into with another user.

                        Accounts. We may suspend an account that breaches these terms.

                        Liability. The app is provided as-is. We are not party to any rental or \
                        living arrangement made between users."""
                },
                ServiceType.FAQ, new String[]{
                        "Frequently Asked Questions",
                        """
                        How is my compatibility score calculated?
                        Your profile is compared with the other person's across three areas — \
                        personality (40%), lifestyle (35%) and habits (25%). The result is shown as \
                        a percentage. Profiles with more detail produce more reliable scores.

                        Why can't I see anyone in Discover?
                        You need to complete your profile first. Only verified users with completed \
                        profiles appear in the deck, and you will not be shown anyone you have \
                        already swiped on.

                        What happens when I like someone?
                        They appear in their pending list. If they like you back, it becomes a match \
                        and a chat opens for both of you.

                        I never received my verification code.
                        Codes expire after five minutes. Use the resend option on the verification \
                        screen, and check your spam folder.

                        Can I change my answers later?
                        Yes. Edit your profile at any time from the Profile tab — your compatibility \
                        scores update automatically.

                        How do I report a problem?
                        Use the Help and Support screen and we will get back to you by email."""
                },
                ServiceType.NOTIFICATION, new String[]{
                        "Notifications",
                        """
                        RoomMate Finder can let you know when something needs your attention:

                        New matches — when someone you liked likes you back.
                        New likes — when another user likes your profile.
                        New messages — when you receive a message in a conversation.
                        Account activity — verification codes and password reset codes, sent by \
                        email.

                        Verification and password reset emails are always sent, because they are \
                        needed to keep your account secure."""
                }
        );
    }
}
