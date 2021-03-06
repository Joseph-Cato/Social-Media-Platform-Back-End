package socialmedia;

import java.io.*;
import java.util.*;

/**
 * The SocialMedia backend. This program provides a backend solution to the ECM1410 OOP CA3 2021 problem.
 *
 * @author Adarsh Prusty
 * @author Joseph Cato
 * @version 1.0
 * @since 13/05/2021
 *
 */
public class SocialMedia implements SocialMediaPlatform {

    public Platform platform;

    /**
     * Social Media method
     * Generates a new, clean Platform object
     */
    public SocialMedia() {

        platform = new Platform();

        // The below sets each counter in every relevant class to 0
        Endorsement.setNumberOfEndorsements(0);

        Post.setNumberOfPosts(0);

        Original.setNumberOfOriginals(0);

        Comment.setTotalNumberOfComments(0);

        Account.setNumberOfAccounts(0);
    }

    @Override
    public int createAccount(String handle) throws IllegalHandleException, InvalidHandleException {

        // Calls other method with blanc description string
        return createAccount(handle, "");
    }

    @Override
    public int createAccount(String handle, String description) throws IllegalHandleException, InvalidHandleException {

        // Checks if handle is valid
        if (handle.equals("") || handle.length() > 30 || handle.contains(" ")) throw new InvalidHandleException();

        // Checks if handle already exists in platform
        if (platform.getAccount(handle) != null) throw new IllegalHandleException();

        //Creates new account
        Account newAccount = new Account(handle, description);

        // Adds account to platform
        platform.addAccount(handle, newAccount);

        // Returns the id of the created account
        return newAccount.getNUMERICAL_IDENTIFIER();

    }

    @Override
    public void removeAccount(int id) throws AccountIDNotRecognisedException {

        // Finds the account with the corresponding Id
        for (Account i : platform.getAccounts().values()) {
            if (i.getNUMERICAL_IDENTIFIER() == id) {
                try {

                    //Removes all posts associated with account

                    HashSet<Endorsement> endorsementsHashSet = i.getEndorsements();
                    HashSet<Comment> commentHashSet = i.getComments();
                    HashSet<Original> originalHashSet = i.getOriginals();

                    for (Endorsement j : endorsementsHashSet) {
                        deletePost(j.getID());
                    }

                    for (Comment j : commentHashSet) {
                        deletePost(j.getID());
                    }

                    for (Original j : originalHashSet) {
                        deletePost(j.getID());
                    }

                    // Removes that account using the other method
                    removeAccount(i.getHandle());
                    // Returns void if this if is evaluated as true (i.e the id is found)
                    return;
                } catch (HandleNotRecognisedException | PostIDNotRecognisedException e) {
                    e.printStackTrace();
                }
            }
        }

        // If the if statement above was never evaluated as true this exception will be thrown
        throw new  AccountIDNotRecognisedException();

    }

    @Override
    public void removeAccount(String handle) throws HandleNotRecognisedException {

        try {

            Account account1 = platform.getAccount(handle);

            // if HashMap.removeAccount() returns null then the value was not found
            // (no account with that handle exists)
            if (account1 == null) throw new HandleNotRecognisedException();

            //Removes all posts associated with account

            HashSet<Endorsement> endorsementsHashSet = account1.getEndorsements();
            HashSet<Comment> commentHashSet = account1.getComments();
            HashSet<Original> originalHashSet = account1.getOriginals();

            for (Endorsement j : endorsementsHashSet) {
                deletePost(j.getID());
            }

            for (Comment j : commentHashSet) {
                deletePost(j.getID());
            }

            for (Original j : originalHashSet) {
                deletePost(j.getID());
            }

            // Removes account from the HashMap accounts
            Account account = platform.removeAccount(handle);

        } catch (PostIDNotRecognisedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void changeAccountHandle(String oldHandle, String newHandle)
            throws HandleNotRecognisedException, IllegalHandleException, InvalidHandleException {

        // Checks if old handle exists in system
        Account account = platform.getAccount(oldHandle);
        if (account == null) throw new HandleNotRecognisedException();

        // Checks if new handle already exists in system
        Account account1 = platform.getAccount(newHandle);
        if (account1 != null) throw new IllegalHandleException();

        // Checks if the new handle is valid
        if (newHandle.equals("") || newHandle.length() > 30 || newHandle.contains(" ")) throw new InvalidHandleException();

        // Removes old data from system
        platform.removeAccount(oldHandle);

        // Changes handle of old account
        account.setHandle(newHandle);

        // Adds new account and handle to system
        platform.addAccount(newHandle, account);

    }

    @Override
    public void updateAccountDescription(String handle, String description) throws HandleNotRecognisedException {

        // Gets account from system, throws exception if user is not found.
        Account account = platform.getAccount(handle);
        if (account == null) throw new HandleNotRecognisedException();

        // Removes account from system
        platform.removeAccount(handle);

        // Changes description of account
        account.setDescription(description);

        // Add account back into system
        platform.addAccount(handle, account);

    }

    @Override
    public String showAccount(String handle) throws HandleNotRecognisedException {

        // The account is retrieved
        Account account = platform.getAccount(handle);

        // If the account is not found the object will be null, so a HandleNotRecognisedException will be thrown
        if (account == null) throw new HandleNotRecognisedException();

        return String.format("""
                ID: %o
                Handle: %s
                Description: %s
                Post count: %o
                Endorse count: %o
                """, account.getNUMERICAL_IDENTIFIER(), account.getHandle(), account.getDescription(),
                account.getTotalPosts(), account.getTotalEndorsementsReceived());
    }

    @Override
    public int createPost(String handle, String message) throws HandleNotRecognisedException, InvalidPostException {

        // Checks if user handle exists on system
        if (!platform.getAccounts().containsKey(handle)) throw new HandleNotRecognisedException();

        // If message is empty or has more than 100 characters is it invalid
        if (message.equals("") || message.length() > 100) throw new InvalidPostException();

        // Creates new original
        Original original = new Original(handle, message);

        // Gets account from platform
        Account account = platform.getAccount(handle);

        // Adds original to account
        account.addOriginal(original);

        // Adds original to platform
        platform.addOriginal(original);

        return original.getID();

    }

    @Override
    public int endorsePost(String handle, int id)
            throws HandleNotRecognisedException, PostIDNotRecognisedException, NotActionablePostException {


        // Checks if the user exists in the system, throws HandleNotRecognisedException otherwise
        if (platform.getAccount(handle) == null) throw new HandleNotRecognisedException();

        // Gets the original post, one of these will be null depending on the type of post
        Original original = platform.getOriginals().get(id);
        Comment comment = platform.getComments().get(id);
        Endorsement endorsement = platform.getEndorsements().get(id);

        if (original != null) {
            // If the post is a original:

            // If post is deleted, NotActionablePostException is thrown
            if (!original.isActionable()) throw new NotActionablePostException();

            // The endorsement object will be created with the original
            endorsement = new Endorsement(handle, original);

            // Original has the endorsement added
            original.addEndorsement(endorsement);

            // Account that posted original has 1 added to it's totalEndorsementsReceived value
            platform.getAccount(original.getHandle()).addEndorsementsReceived();

        } else if (comment != null) {

            // If the post is a comment:

            // If post is deleted, NotActionablePostException is thrown
            if (!comment.isActionable()) throw new NotActionablePostException();

            // The endorsement object will be created with the comment
            endorsement = new Endorsement(handle, comment);

            // Comment has the endorsement added
            comment.addEndorsement(endorsement);

            // Account that posted comment has 1 added to it's totalEndorsementsReceived value
            platform.getAccount(comment.getHandle()).addEndorsementsReceived();

        } else if (endorsement != null){

            // If the post is an endorsement a NotActionablePostException is thrown
            throw new NotActionablePostException();
        } else {

            // If no post object with the specified id is found a PostIDNotRecognisedException is thrown
            throw new PostIDNotRecognisedException();
        }

        // Gets account that is endorsing
        Account account = platform.getAccount(handle);

        // Adds endorsement to account
        account.addEndorsement(endorsement);

        // Adds endorsement to system
        platform.addEndorsement(endorsement);


        return endorsement.getID();

    }

    @Override
    public int commentPost(String handle, int id, String message) throws HandleNotRecognisedException,
            PostIDNotRecognisedException, NotActionablePostException, InvalidPostException {

        // If message is empty or greater than 100 character a InvalidPostException is thrown
        if (message.equals("") || message.length() > 100) throw new InvalidPostException();

        // Account is retrieved from the platform, if no account is found a HandleNotRecognisedException is thrown
        Account account = platform.getAccount(handle);
        if (account == null) throw new HandleNotRecognisedException();

        Comment newComment;

        // Gets the post from the systems collections, these objects will be null if they are the wrong type
        Comment comment = platform.getComments().get(id);
        Original original = platform.getOriginals().get(id);
        Endorsement endorsement = platform.getEndorsements().get(id);

        if (comment != null) {
            // If the post is a comment:

            // If post is deleted, NotActionablePostException is thrown
            if (!comment.isActionable()) throw new NotActionablePostException();

            // An appropriate Comment object is created
            newComment = new Comment(handle, comment, message);

            // comment has new comment added
            comment.addComment(newComment);

        } else if (original != null) {
            // If the post is an Original:

            // If post is deleted, NotActionablePostException is thrown
            if (!original.isActionable()) throw new NotActionablePostException();

            // An appropriate Comment object is created
            newComment = new Comment(handle, original, message);

            // Original has new comment added
            original.addComment(newComment);

        } else if (endorsement != null) {

            // If the post is an endorsement a NotActionablePostException is thrown
            throw new NotActionablePostException();
        } else {

            // If the post is not found in the system a PostIDNotRecognisedException is thrown
            throw new PostIDNotRecognisedException();
        }

        // Comment is added to account
        account.addComment(newComment);

        // Comment is added to platform
        platform.addComment(newComment);

        return newComment.getID();
    }

    @Override
    public void deletePost(int id) throws PostIDNotRecognisedException {

        // One of these variables will be not null depending on the type of object the post is
        Original original = platform.getOriginals().get(id);
        Comment comment = platform.getComments().get(id);
        Endorsement endorsement = platform.getEndorsements().get(id);


        if (original != null) {

            // Gets original posting account
            Account account = platform.getAccount(original.getHandle());

            // To avoid concurrent modification, endorsements to be removed are added to a HashSet
            HashSet<Endorsement> endorsementsToRemoveHashSet = original.getEndorsements();

            // So objects can be added the HashSet is converted to an ArrayList
            ArrayList<Endorsement> endorsementsToRemove = new ArrayList<>(endorsementsToRemoveHashSet);


            endorsementsToRemove.addAll(account.getEndorsements());

            // Endorsements from list endorsementsToRemove are removed from the comment and account
            for (Endorsement i : endorsementsToRemove) {

                // Endorsement is removed from account
                account.removeEndorsement(i);

                // Endorsement is removed from system
                platform.removeEndorsement(i);

                // Endorsed account has 1 taken of its totalEndorsementsReceived value
                platform.getAccount( original.getHandle() ).removeEndorsementsReceived();


            }

            // Removes original from Account
            account.removeOriginal(original);

            original.deletePost();

        } else if (comment != null) {

            // Gets original posting account
            Account account = platform.getAccount(comment.getHandle());

            // To avoid concurrent modification, endorsements to be removed are added to a HashSet
            HashSet<Endorsement> endorsementsToRemoveHashSet = comment.getEndorsements();

            // So objects can be added the HashSet is converted to an ArrayList
            ArrayList<Endorsement> endorsementsToRemove = new ArrayList<>(endorsementsToRemoveHashSet);

            // Endorsements from list endorsementsToRemove are removed from the comment and account
            for (Endorsement i : endorsementsToRemove) {

                Account endorsementAccount =  platform.getAccount(i.getHandle());

                // Endorsement is removed from account
                endorsementAccount.removeEndorsement(i);

                // Endorsement is removed from system
                platform.removeEndorsement(i);

                // Endorsed account has 1 taken of its totalEndorsementsReceived value
                platform.getAccount( comment.getHandle() ).removeEndorsementsReceived();

            }

            // Removes comment from Account
            account.removeComment(comment);

            comment.deletePost();

        } else if (endorsement != null) {

            // Original is retrieved
            Original endorsedOriginal = platform.getOriginals().get( endorsement.getEndorsedPost().getID() );
            Comment endorsedComment = platform.getComments().get( endorsement.getEndorsedPost().getID() );

            // The type of object will be tested and one removed from its numberOfEndorsements variable
            if (endorsedOriginal != null) {

                // The account that was endorsed will have 1 subtracted from the totalEndorsementsReceived value
                Account account = platform.getAccounts().get( endorsedOriginal.getHandle() );
                account.removeEndorsementsReceived();

                // Endorsed object is removed from the accounts list of endorsements
                endorsedOriginal.removeEndorsement(endorsement);

            } else if (endorsedComment != null) {

                // The account that was endorsed will have 1 subtracted from the totalEndorsements value
                Account account = platform.getAccounts().get( endorsedComment.getHandle() );
                account.removeEndorsementsReceived();

                // Endorsed object is removed from the accounts list of endorsements
                endorsedComment.removeEndorsement(endorsement);

            }

            // Endorsement is removed from account
            platform.getAccount(endorsement.getHandle()).removeEndorsement(endorsement);

            endorsement.deletePost();

        } else {

            // If id does not match any post object type then it does not exist, PostIDNotRecognisedException is thrown
            throw new PostIDNotRecognisedException();
        }

    }

    @Override
    public String showIndividualPost(int id) throws PostIDNotRecognisedException {

        // The post object is assigned to one of these variables depending on the object type
        Original original = platform.getOriginals().get(id);
        Comment comment = platform.getComments().get(id);
        Endorsement endorsement = platform.getEndorsements().get(id);

        String output;

        // The output string will be constructed based on what object type the post is
        if (original != null) {

            output = String.format("""
                ID: %d
                Account: %s
                No. endorsements: %d | No. comments: %d
                %s
                """, original.getID(), original.getHandle(), original.getNumberOfEndorsements(), original.getNumberOfComments(), original.getMessage());
        } else if (comment != null) {

            output = String.format("""
                ID: %d
                Account: %s
                No. endorsements: %d | No. comments: %d
                %s
                """, comment.getID(), comment.getHandle(), comment.getNumberOfEndorsements(), comment.getNumberOfComments(), comment.getMessage());
        } else if (endorsement != null) {

            output = String.format("""
                    ID: %d
                    Account: %s
                    No. endorsements: 0 | No. comments: 0
                    %s
                    """, endorsement.getID(), endorsement.getHandle(), endorsement.getMessage());
        } else {

            // if all objets are null the post has not been found in the system so a PostIDNotRecognisedException will be thrown
            throw new PostIDNotRecognisedException();
        }

        return output;

    }

    @Override
    public StringBuilder showPostChildrenDetails(int id)
            throws PostIDNotRecognisedException, NotActionablePostException {

        // String builder object that will be the final output of the function
        StringBuilder finalOutput = new StringBuilder();

        // The post is assumed to be an original and retrieved
        Original original = platform.getOriginals().get(id);

        // If the object is null the post is assumed to be a comment
        if (original == null) {
            Comment comment = platform.getComments().get(id);

            // Checks if post is still null, implying it is an endorsement
            if (comment == null) {

                if (platform.getEndorsements().get(id) != null ) {

                    // If the if the ID refers to an endorsement post (is not null), a NotActionablePostException is thrown
                    throw new NotActionablePostException();
                } else {

                    // If it is not an endorsement (is null), the post does not exist in the system, so a PostIDNotRecognisedException is thrown
                    throw new PostIDNotRecognisedException();
                }
            }

            //---------This part of the method will only run if the post is a comment---------

            finalOutput.append(showIndividualPost(comment.getID()));
            finalOutput.append("|\n");

            HashSet<Comment> commentsHashSet = comment.getComments();

            // List is sorted with CommentComparator object
            ArrayList<Comment> commentsList = new ArrayList<>(commentsHashSet);
            CommentComparator commentComparator = new CommentComparator();
            commentsList.sort(commentComparator);

            for (Comment i: commentsList) {
                finalOutput.append(showPostChildrenDetails(i.getID(), 4));
                finalOutput.append("\n");
            }

            finalOutput.deleteCharAt(finalOutput.length()-1);

            return finalOutput;

        }

        //---------This part of the method will only run if the post is an original---------

        // Checks if original is actionable (has not been deleted)
        if (!original.isActionable()) throw new NotActionablePostException();

        finalOutput.append(showIndividualPost(original.getID()));
        finalOutput.append("|\n");

        HashSet<Comment> commentsHashSet = original.getComments();

        // List is sorted with CommentComparator object
        ArrayList<Comment> commentsList = new ArrayList<>(commentsHashSet);
        CommentComparator commentComparator = new CommentComparator();
        commentsList.sort(commentComparator);


        for (Comment i: commentsList) {
            finalOutput.append(showPostChildrenDetails(i.getID(), 4));
            finalOutput.append("\n");
        }

        finalOutput.deleteCharAt(finalOutput.length()-1);

        return finalOutput;

    }

    public StringBuilder showPostChildrenDetails(int id, int spacing) throws NotActionablePostException, PostIDNotRecognisedException {

        Comment comment = platform.getComments().get(id);

        if (!comment.isActionable()) throw new NotActionablePostException();

        StringBuilder output = new StringBuilder();

        // String builders are created based for the indentation

        StringBuilder secondaryIndentation = new StringBuilder();
        secondaryIndentation.append(" ".repeat(Math.max(0, spacing)));

        String postDetails = showIndividualPost(id);
        String[] splitPostDetails = postDetails.split("\n");

        String firstIndentation = " ".repeat(Math.max(0, spacing - 4)) +
                "| > ";
        splitPostDetails[0] = firstIndentation + splitPostDetails[0];
        for (int i=1; i< splitPostDetails.length; i++) {
            splitPostDetails[i] = secondaryIndentation + splitPostDetails[i];
        }

        output.append(String.join("\n", splitPostDetails));

        HashSet<Comment> commentsHashSet = comment.getComments();

        if (!commentsHashSet.isEmpty()) {
            output.append("\n").append(secondaryIndentation).append("|\n");
        } else {
            output.append("\n" );
        }

        // List is sorted with CommentComparator object
        ArrayList<Comment> commentsList = new ArrayList<>(commentsHashSet);
        CommentComparator commentComparator = new CommentComparator();
        commentsList.sort(commentComparator);

        for (Comment i: commentsList) {
            output.append(showPostChildrenDetails(i.getID(), spacing+4));
        }

        return output;
    }


    @Override
    public int getNumberOfAccounts() {

        return Account.getNumberOfAccounts();

    }

    @Override
    public int getTotalOriginalPosts() {

        return Original.getNumberOfPosts();
    }

    @Override
    public int getTotalEndorsmentPosts() {

        return Endorsement.getNumberOfEndorsements();
    }

    @Override
    public int getTotalCommentPosts() {

        return Comment.getTotalNumberOfComments();
    }

    @Override
    public int getMostEndorsedPost() {

        Original original = platform.getOriginals().get(1);
        Comment comment = platform.getComments().get(1);

        // If there is at least one original
        if (original != null) {

            // All originals will be tested
            for (Original i : platform.getOriginals().values()) {

                // If i is has more endorsements then original, original will be set as i
                if (i.getNumberOfEndorsements() > original.getNumberOfEndorsements()) {
                    original = i;
                }
            }
        }

        // If there is at least one comment
        if (comment != null) {

            // All comments will be tested
            for (Comment i : platform.getComments().values()) {

                // If i has more endorsements than comment, comment will be set as i
                if (i.getNumberOfEndorsements() > comment.getNumberOfEndorsements()) {
                    comment = i;
                }
            }
        }

        // 0 will be returned if there are no posts
        // If one is null and the other is not, the not not null objects ID will be returned
        if (original == null && comment == null) {
            return 0;
        } else if (original != null && comment == null) {
            return original.getID();
        } else if (original == null) {
            return comment.getID();
        }

        // The most endorsed out of comment and original will be returned
        if (original.getNumberOfEndorsements() > comment.getNumberOfEndorsements()) {
            return original.getID();
        } else {
            return comment.getID();
        }

    }

    @Override
    public int getMostEndorsedAccount() {

        Map.Entry<String, Account> mostEndorsedAccount = platform.getAccounts().entrySet().stream().toList().get(0);

        // Accounts are checked
        for (Map.Entry<String, Account> i : platform.getAccounts().entrySet()) {

            // If i has more endorsements than mostEndorsedAccount, mostEndorsedAccount is set as i
            if (i.getValue().getTotalEndorsementsReceived() > mostEndorsedAccount.getValue().getTotalEndorsementsReceived()) {
                mostEndorsedAccount = i;
            }
        }

        // The ID of the account with most endorsements is returned
        return mostEndorsedAccount.getValue().getNUMERICAL_IDENTIFIER();

    }

    @Override
    public void erasePlatform() {
        // Calls various classes in Platform to erase the HashMaps and reset counters in other classes
        platform.eraseHashMaps();
        platform.clearCounters();

    }

    @Override
    public void savePlatform(String filename) throws IOException {
        try {
            platform.saveCounters(); // This calls a method that saves all counters to variables in platform

            FileOutputStream fileOut = new FileOutputStream(filename); // Creates a new FOS using the filename
            ObjectOutputStream out = new ObjectOutputStream(fileOut); // Creates an OOS to write objects to file
            out.writeObject(platform); // Writes the object
            // Below closes the OOS and FOS
            out.close();
            fileOut.close();
        } catch (IOException exception) {
            throw new IOException();
        }


    }

    @Override
    public void loadPlatform(String filename) throws IOException, ClassNotFoundException {
        try {
            FileInputStream fileIn = new FileInputStream(filename); // Creates FIS using the filename
            ObjectInputStream in = new ObjectInputStream(fileIn); // Creates an OIS to read objects from file
            platform = (Platform) in.readObject(); // Reads the Platform object from file
            // Below closes the OIS and FIS
            in.close();
            fileIn.close();

            platform.loadCounters(); // This calls a method that loads all counters from variables in new platform

        } catch (IOException i) {
            i.printStackTrace();
            throw new IOException();
        } catch (ClassNotFoundException c) {
            System.out.println("Platform class not found");
            c.printStackTrace();
            throw new ClassNotFoundException();
        }

    }

}