#
# Dockerfile for a checking account service
#

FROM java:openjdk-8-jre
MAINTAINER João Eduardo F. Bertacchi <joaobertacchi@gmail.com>
ENV REFRESHED_AT 2017-09-25

ADD checking_account_service-0.1.0-standalone.jar /

ENV PORT 3000

EXPOSE 3000

CMD ["java", "-jar", "checking_account_service-0.1.0-standalone.jar"]