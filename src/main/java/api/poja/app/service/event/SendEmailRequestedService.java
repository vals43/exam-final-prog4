package api.poja.app.service.event;

import api.poja.app.endpoint.event.model.SendEmailRequested;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.mail.Email;
import api.poja.app.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SendEmailRequestedService implements Consumer<SendEmailRequested> {
  private final Mailer mailer;
  private final BucketComponent bucketComponent;

  @SneakyThrows
  @Override
  public void accept(SendEmailRequested sendEmailRequested) {
    var recipientAddress = new InternetAddress(sendEmailRequested.getTo());
    List<java.io.File> attachments =
        sendEmailRequested.getBucketKey() == null
            ? List.of()
            : List.of(bucketComponent.download(sendEmailRequested.getBucketKey()));
    mailer.accept(
        new Email(
            recipientAddress,
            List.of(),
            List.of(),
            sendEmailRequested.getSubject(),
            sendEmailRequested.getHtmlBody(),
            attachments));
  }
}
