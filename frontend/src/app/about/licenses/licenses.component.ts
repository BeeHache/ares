import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Dependency {
  name: string;
  version?: string;
  license: string;
  url?: string;
}

interface Category {
  name: string;
  dependencies: Dependency[];
}

@Component({
  selector: 'app-licenses',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './licenses.component.html',
  styleUrl: './licenses.component.css'
})
export class LicensesComponent {
  projectLicense = `MIT License

Copyright (c) 2026 Benjamin King

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.`;

  categories: Category[] = [
    {
      name: 'Infrastructure & Services',
      dependencies: [
        { name: 'dpage/pgadmin4', license: 'PostgreSQL License', url: 'https://www.pgadmin.org/' },
        { name: 'Mailhog', license: 'MIT License', url: 'https://github.com/mailhog/MailHog' },
        { name: 'Nginx', version: 'alpine', license: 'BSD 2-Clause', url: 'https://hub.docker.com/_/nginx' },
        { name: 'PostgreSQL', version: '15-alpine', license: 'PostgreSQL License', url: 'https://hub.docker.com/_/postgres' },
        { name: 'Redis', version: '7-alpine', license: 'BSD 3-Clause', url: 'https://hub.docker.com/_/redis' }
      ]
    },
    {
      name: 'Spring Boot & Core',
      dependencies: [
        { name: 'Spring Boot', version: '4.0.1', license: 'Apache License 2.0', url: 'https://spring.io/projects/spring-boot' },
        { name: 'Spring Boot Web Starter', license: 'Apache License 2.0', url: 'https://spring.io/projects/spring-boot' },
        { name: 'Spring Boot Security Starter', license: 'Apache License 2.0', url: 'https://spring.io/projects/spring-security' },
        { name: 'Spring Boot Data JPA Starter', license: 'Apache License 2.0', url: 'https://spring.io/projects/spring-data-jpa' },
        { name: 'Spring Boot Mail Starter', license: 'Apache License 2.0', url: 'https://spring.io/projects/spring-boot' },
        { name: 'Spring Boot Thymeleaf Starter', license: 'Apache License 2.0', url: 'https://spring.io/projects/spring-boot' },
        { name: 'Spring Data Redis', license: 'Apache License 2.0', url: 'https://spring.io/projects/spring-data-redis' },
        { name: 'Flyway Core', version: '12.0.0', license: 'Apache License 2.0', url: 'https://flywaydb.org/' }
      ]
    },
    {
      name: 'Data & Database',
      dependencies: [
        { name: 'Hibernate Community Dialects', license: 'LGPL 2.1', url: 'https://hibernate.org/' },
        { name: 'Jedis', version: '7.2.0', license: 'MIT License', url: 'https://github.com/redis/jedis' },
        { name: 'PostgreSQL JDBC Driver', version: '42.7.8', license: 'PostgreSQL License', url: 'https://jdbc.postgresql.org/' }
      ]
    },
    {
      name: 'Utilities & Libraries',
      dependencies: [
        { name: 'Angular', version: '21.0.0', license: 'MIT License', url: 'https://angular.io/' },
        { name: 'Jackson Databind', version: '3.0.4', license: 'Apache License 2.0', url: 'https://github.com/FasterXML/jackson-databind' },
        { name: 'JJWT (Java JWT)', version: '0.13.0', license: 'Apache License 2.0', url: 'https://github.com/jwtk/jjwt' },
        { name: 'opml-parser', version: '3.1.0', license: 'Apache License 2.0', url: 'https://github.com/mdewilde/opml-parser' },
        { name: 'Project Lombok', version: '1.18.42', license: 'MIT License', url: 'https://projectlombok.org/' },
        { name: 'rss-reader', version: '3.12.0', license: 'MIT License', url: 'https://github.com/w3stling/rssreader' },
        { name: 'Togglz', version: '4.6.0', license: 'Apache License 2.0', url: 'https://www.togglz.org/' }
      ]
    },
    {
      name: 'Testing',
      dependencies: [
        { name: 'Spring Boot Test Starter', license: 'Apache License 2.0', url: 'https://spring.io/projects/spring-boot' },
        { name: 'Testcontainers', version: '1.21.4', license: 'MIT License', url: 'https://www.testcontainers.org/' }
      ]
    }
  ];
}
