import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, concat } from 'rxjs/operators';
import 'rxjs/Rx';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { map } from 'rxjs/operator/map';


@Injectable()
export class DataService {
    constructor(private httpClient: HttpClient) { }
    

    /* getData(): Promise<any> {
    return Promise.resolve(this.httpClient.get('http://localhost:4040/movies').map(
        (data) => {console.log(data)},
        (error) => {return error;}   
    )).then((result) => {
        return result; 
        }).catch((err) => {
            
        });
    } */

}