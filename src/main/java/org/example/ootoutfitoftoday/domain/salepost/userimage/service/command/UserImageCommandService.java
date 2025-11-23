package org.example.ootoutfitoftoday.domain.salepost.userimage.service.command;

import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.salepost.userimage.entity.UserImage;

public interface UserImageCommandService {

    void softDeleteUserImage(UserImage userImage);

    UserImage createAndSave(Image image);
}